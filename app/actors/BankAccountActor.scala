package actors

import actors.WithdrawWorkerActor.{InsufficientFunds, StartWithdraw, WithdrawSuccessful}
import akka.actor.{Actor, ActorLogging, Props, Stash}
import akka.pattern.pipe
import commands.{DepositMoney, GetCurrentBalance, ListMostRecentTransactions, WithdrawMoney}
import dao._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object BankAccountActor{



  def props(bankAccountDAO: BankAccountDAO): Props = {
    Props(new BankAccountActor(bankAccountDAO))
  }
}

class BankAccountActor(bankAccountDAO: BankAccountDAO) extends Actor with Stash with ActorLogging {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception                ⇒ Restart
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

    case l: ListMostRecentTransactions => bankAccountDAO.listTransactions(l.clientId).pipeTo(self)(sender())
    case t: Transactions => sender() ! t
    case gcd: GetCurrentBalance => bankAccountDAO.getBalance(gcd.clientId).pipeTo(self)(sender())
    case b:Balance => sender() ! b

  }

  def working: Receive = {
    case m:TransactionComplete =>
      context.unbecome()
      unstashAll()
      sender() ! m
    case m: WithdrawSuccessful =>
      context.unbecome()
      unstashAll()
      m.orig ! m.transactionComplete
    case m: InsufficientFunds =>
      context.unbecome()
      unstashAll()
      m.orig ! m
    case _ ⇒ stash()
  }
}

