package actors

import actors.BankAccountActor.{DepositMoney, GetCurrentBalance, ListMostRecentTransactions, WithdrawMoney}
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import dao.BankAccountDAO

import scala.concurrent.duration._

object BankTransactionSupervisorActor{
  def props(bankAccountDAO: BankAccountDAO): Props ={
    Props(new BankTransactionSupervisorActor(bankAccountDAO))
  }
}

class BankTransactionSupervisorActor(bankAccountDAO: BankAccountDAO) extends Actor with ActorLogging{

  val bankAccountActor = context.actorOf(BankAccountActor.props(bankAccountDAO), "bank-account-actor")

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception                ⇒ Restart
    }

  override def receive: Receive = {
    case d: DepositMoney => bankAccountActor.forward(d)
    case w: WithdrawMoney => bankAccountActor.forward(w)
    case l: ListMostRecentTransactions => bankAccountActor.forward(l)
    case gcd: GetCurrentBalance => bankAccountActor.forward(gcd)
  }
}
