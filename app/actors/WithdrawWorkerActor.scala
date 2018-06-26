package actors

import java.util.UUID

import actors.WithdrawWorkerActor.{InsufficientFunds, StartWithdraw, WithdrawSuccessful}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import dao.{Balance, BankAccountDAO, TransactionComplete}

import scala.concurrent.ExecutionContext.Implicits.global

object WithdrawWorkerActor{
  case class StartWithdraw(clientId: String, amount: BigDecimal, orig: ActorRef)
  case class WithdrawSuccessful(transactionComplete: TransactionComplete, orig: ActorRef)
  case class InsufficientFunds(clientId: String, amountDesired: BigDecimal, orig: ActorRef)

  def props(bankAccountDAO: BankAccountDAO): Props = {
    Props(new WithdrawWorkerActor(bankAccountDAO))
  }
}

class WithdrawWorkerActor(bankAccountDAO: BankAccountDAO) extends Actor with ActorLogging{
  override def receive: Receive = {
    case sw: StartWithdraw => {
      bankAccountDAO.getBalance(sw.clientId).pipeTo(self)(sender())
      context.become(waitingForBalance(sw.orig, sw))
    }
  }

  def waitingForBalance(orig: ActorRef, sw: StartWithdraw):Receive = {
    case b:Balance => {
      if(b.amount >= sw.amount){
        bankAccountDAO.withdraw(sw.amount, sw.clientId).pipeTo(self)(sender())
        context.become(waitingForWithdraw(orig))
      }else{
        sender() ! InsufficientFunds(sw.clientId, sw.amount, orig)
      }
    }
  }

  def waitingForWithdraw(orig: ActorRef):Receive = {
    case tc:TransactionComplete => {
      sender() ! WithdrawSuccessful(tc, orig)
      finish()
    }
  }

  def finish(): Unit ={
    context stop self
  }
}

