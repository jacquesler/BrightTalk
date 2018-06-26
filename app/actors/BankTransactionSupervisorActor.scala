package actors

import actors.UserActor.Login
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import commands.{DepositMoney, GetCurrentBalance, ListMostRecentTransactions, WithdrawMoney}
import dao.BankAccountDAO

import scala.concurrent.duration._

object BankTransactionSupervisorActor{
  def props(bankAccountDAO: BankAccountDAO): Props ={
    Props(new BankTransactionSupervisorActor(bankAccountDAO))
  }
}

class BankTransactionSupervisorActor(bankAccountDAO: BankAccountDAO) extends Actor with ActorLogging{

  val bankAccountActor = context.actorOf(BankAccountActor.props(bankAccountDAO), "bank-account-actor")
  val userActor = context.actorOf(UserActor.props(bankAccountDAO), "user-actor")

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception                ⇒ Restart
    }

  override def receive: Receive = {
    case d: DepositMoney => bankAccountActor.forward(d)
    case w: WithdrawMoney => bankAccountActor.forward(w)
    case l: ListMostRecentTransactions => bankAccountActor.forward(l)
    case gcd: GetCurrentBalance => bankAccountActor.forward(gcd)
    case l: Login => userActor.forward(l)
  }
}
