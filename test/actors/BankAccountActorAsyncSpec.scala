package actors

import actors.BankAccountActor._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import dao.MockBankAccountDAO
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class BankAccountActorAsyncSpec(actorSystem: ActorSystem) extends TestKit(actorSystem) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll{

  behavior of s"Integration tests of ${classOf[BankAccountActor].getSimpleName}"
  implicit val timeout = Timeout(1 seconds)
  def this() = this(ActorSystem("BankAccountSpec"))

  val clientId = "jacquesja"

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val accountActor = system.actorOf(BankAccountActor.props(MockBankAccountDAO()))

  it should "be able to perform many deposits and withdraws and give the correct balance" in {
    val testProbe = TestProbe()
    accountActor ! DepositMoney(500, clientId)
    accountActor ! DepositMoney(1000, clientId)
    accountActor ! WithdrawMoney(1500, clientId)
    accountActor ! DepositMoney(500, clientId)
    accountActor.tell(GetCurrentBalance(clientId), testProbe.ref)
    testProbe.expectMsg(1 second, Balance(600, clientId))
  }
}



