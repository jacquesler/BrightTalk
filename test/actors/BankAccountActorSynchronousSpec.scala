package actors

import actors.BankAccountActor._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import dao.MockBankAccountDAO
import org.scalatest.{BeforeAndAfter, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class BankAccountActorSynchronousSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfter{
  behavior of s"Unit tests of ${classOf[BankAccountActor].getSimpleName}"

  val clientId = "jacquesja"

  it should("be able to deposit an amount in the account of a user") in {
    val bankAccountActor = TestActorRef[BankAccountActor](BankAccountActor.props(MockBankAccountDAO()))
    bankAccountActor ! DepositMoney(500, clientId)
    expectMsgType[TransactionSuccessful](1 second)
  }

  it should("be able to get an account balance for user") in {
    val bankAccountActor = TestActorRef[BankAccountActor](BankAccountActor.props(MockBankAccountDAO()))
    bankAccountActor ! GetCurrentBalance(clientId)
    expectMsg(1 second, Balance(500, clientId))
  }

  it should("be able to deposit an amount and get the correct account balance for user") in {
    val bankAccountActor = TestActorRef[BankAccountActor](BankAccountActor.props(MockBankAccountDAO()))
    bankAccountActor ! DepositMoney(500, clientId)
    expectMsgType[TransactionSuccessful](1 second)
    bankAccountActor ! GetCurrentBalance(clientId)
    expectMsg(1 second, Balance(1000, clientId))
  }

  it should("be able to withdraw money if user has enough money") in {
    val bankAccountActor = TestActorRef[BankAccountActor](BankAccountActor.props(MockBankAccountDAO()))
    bankAccountActor ! WithdrawMoney(50, clientId)
    expectMsgType[TransactionSuccessful](1 second)
  }

  it should("to list a number of recent transactions") in {
    val bankAccountActor = TestActorRef[BankAccountActor](BankAccountActor.props(MockBankAccountDAO()))
    bankAccountActor ! DepositMoney(500, clientId)
    bankAccountActor ! ListMostRecentTransactions(2, clientId)
    expectMsgType[Statement](1 second)
  }



}
