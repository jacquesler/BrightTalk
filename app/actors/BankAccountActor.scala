package actors

import java.time.LocalDateTime
import java.util.UUID

import actors.BankAccountActor._
import actors.WithdrawWorkerActor.{InsufficientFunds, StartWithdraw, WithdrawSuccessful}
import akka.actor.{Actor, ActorLogging, Props, Stash}
import akka.pattern.pipe
import dao._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Bank Account actor used to interact with an bankAccount DAO. This uses a mocked DAO with a map simulating a db
  * The Map is not thread safe but all interaction with DAO is within the Actor keeping our state correct
  * The DAO case classes look similar to the Bank Account Actor case classes, but I do not want to leak the structure of my DOA to other components in case it changes
  * depending on the underlying Data Base or mock. This way only the actor has to change if there is a fundamental change in the underlying DB return structure
  */

object BankAccountActor{

  sealed trait AccountActivity
  case class DepositMoney(amount: BigDecimal, clientId: String)
  case class WithdrawMoney(amount: BigDecimal, clientId: String)
  case class GetCurrentBalance(clientId: String)
  case class ListMostRecentTransactions(nr: Int, clientId: String)
  case class Balance(amount: BigDecimal, clientId: String)
  case class TransactionSuccessful(refNumber: UUID, clientId: String)
  case class StatementLineItem(refNumber: UUID, amount: BigDecimal, datetime: LocalDateTime)
  case class Statement(statementLineItems: List[StatementLineItem], clientId: String)

  def props(bankAccountDAO: BankAccountDAO): Props = {
    Props(new BankAccountActor(bankAccountDAO))
  }
}

class BankAccountActor(bankAccountDAO: BankAccountDAO) extends Actor with Stash with ActorLogging {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception                ⇒ Escalate
    }

  override def receive: Receive = {
    case d: DepositMoney => {
      context.become(working, discardOld = false)
      bankAccountDAO.deposit(d.amount, d.clientId).pipeTo (self)(sender())
    }

    case w: WithdrawMoney => {
      val withdrawWorker = context.actorOf(WithdrawWorkerActor.props(bankAccountDAO), "withdraw-worker-actor")
      context.become(working, discardOld = false)
      withdrawWorker ! (StartWithdraw(w.clientId, w.amount, sender()))
    }

    case l: ListMostRecentTransactions => bankAccountDAO.listTransactions(l.nr, l.clientId).pipeTo(self)(sender())

    case t: Transactions => sender() ! Statement(t.transactions.map(transaction =>
      StatementLineItem(transaction.refNumber, transaction.amount, transaction.datetime)), t.clientId)

    case gcd: GetCurrentBalance => bankAccountDAO.getBalance(gcd.clientId).pipeTo(self)(sender())

    case b:TransactionBalance => sender() ! Balance(b.amount, b.clientId)

  }

  def working: Receive = {
    case m:TransactionComplete =>
      context.unbecome()
      unstashAll()
      sender() ! TransactionSuccessful(m.refNumber, m.clientId)
    case m: WithdrawSuccessful =>
      context.unbecome()
      unstashAll()
      m.orig ! TransactionSuccessful(m.transactionComplete.refNumber, m.transactionComplete.clientId)
    case m: InsufficientFunds =>
      context.unbecome()
      unstashAll()
      m.orig ! m
    case _ ⇒ stash()
  }
}

