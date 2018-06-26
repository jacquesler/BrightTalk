package actors

import actors.UserActor.{Login, LoginSuccessful, UserDoesNotExist}
import akka.actor.{Actor, ActorLogging, Props}
import dao.BankAccountDAO

object UserActor{
  case class Login(username: String)
  case object LoginSuccessful
  case object UserDoesNotExist

  def props(bankAccountDAO: BankAccountDAO): Props = {
    Props(new UserActor(bankAccountDAO))
  }
}
class UserActor(bankAccountDAO: BankAccountDAO) extends Actor with ActorLogging{
  override def receive: Receive = {
    case m: Login => {
      if(bankAccountDAO.isUser(m.username))
        LoginSuccessful
      else
        UserDoesNotExist
    }
  }
}
