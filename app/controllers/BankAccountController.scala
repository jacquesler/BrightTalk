package controllers

import actors.BankTransactionSupervisorActor
import actors.WithdrawWorkerActor.InsufficientFunds
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import commands.{DepositMoney, GetCurrentBalance, ListMostRecentTransactions, WithdrawMoney}
import dao.{Balance, MockBankAccountDAO, TransactionComplete, Transactions}
import javax.inject._
import models.{Deposit, Withdraw}
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class BankAccountController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {
  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */

  implicit val timeout = Timeout(10 seconds)
  val bankAccountActor = actorSystem.actorOf(BankTransactionSupervisorActor.props(MockBankAccountDAO()), "bank-transaction-supervisor")

  def deposit = Action.async {
      request =>
        val json = request.body.asJson.get
        val deposit = reads(json)
        val clientID = deposit.clientId


        
        val result = for{
          transactionComplete <- bankAccountActor ? DepositMoney(deposit.amount, clientID)
          currentBalance <- bankAccountActor ? GetCurrentBalance(clientID)
        }yield{
          (transactionComplete.asInstanceOf[TransactionComplete],  currentBalance.asInstanceOf[Balance])
        }

        result.map{case (transactionComplete, currentBalance) => Ok(
          "Reference number" + transactionComplete.refNumber + " " +
          "ClientId " + transactionComplete.clientId + " " +
          "Current Balance" + currentBalance.amount)
        }

  }

  def withdraw = Action.async {
    request =>

      val json = request.body.asJson.get
      val withdraw = readsWithdraw(json)
      val clientID = withdraw.clientId

      val result = for{
        withdraw <- bankAccountActor ? WithdrawMoney(withdraw.amount, clientID)
        currentBalance <- bankAccountActor ? GetCurrentBalance(clientID)
      }
        yield{
          val balance = currentBalance.asInstanceOf[Balance]
          val x = withdraw match {
            case m:InsufficientFunds =>
              "InsufficientFunds for " + m.amountDesired + " only have " + balance.amount
            case m: TransactionComplete =>
              "You drew money with reference number " + m.refNumber + "current balance is " + balance.amount
          }
          x
        }
        result.map{msg => Ok(msg)
      }
  }

  def listTransactions(clientId: String) = Action.async {
    request =>
      val result = bankAccountActor ? ListMostRecentTransactions(Int.MaxValue, clientId)
      result.map{transactions => Ok(transactions.asInstanceOf[Transactions].transactions.mkString(" "))}
  }

  def currentBalance(clientId: String) = Action.async {
    request =>
    val result = bankAccountActor ? GetCurrentBalance(clientId)
    result.map{balance => Ok(balance.toString)}
  }

  def reads(json: JsValue): Deposit = {
    val clientId = (json \ "clientId").as[String]
    val amount = (json \ "amount").as[BigDecimal]
    Deposit(clientId, amount)
  }

  def readsWithdraw(json: JsValue): Withdraw = {
    val clientId = (json \ "clientId").as[String]
    val amount = (json \ "amount").as[BigDecimal]
    Withdraw(clientId, amount)
  }
}
