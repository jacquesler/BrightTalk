package controllers

import actors.BankAccountActor._
import actors.WithdrawWorkerActor.InsufficientFunds
import actors.{BankAccountActor, BankTransactionSupervisorActor}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import controllers.jsonTransformers.BankAccountJsonTransformer._
import dao.MockBankAccountDAO
import javax.inject._
import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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

  implicit val timeout = Timeout(5 seconds)
  val bankAccountActor = actorSystem.actorOf(BankTransactionSupervisorActor.props(MockBankAccountDAO()), "bank-transaction-supervisor")



  def deposit = Action.async {
      request =>
        implicit val successWriter = Json.writes[SuccessfulDepositDTO]
        implicit val badRequestWriter = Json.writes[BadRequestDTO]
        request.body.asJson.get.validate[DepositDTO] match {
          case s: JsSuccess[DepositDTO] => {
              val deposit = s.value
              val result = for{
                transactionComplete <- bankAccountActor ? DepositMoney(deposit.amount, deposit.clientId)
                currentBalance <- bankAccountActor ? GetCurrentBalance(deposit.clientId)
              }yield{
                (transactionComplete.asInstanceOf[TransactionSuccessful],  currentBalance.asInstanceOf[Balance])
              }

            result.map{case (tc, currentBalance) =>
               Ok(Json.toJson(SuccessfulDepositDTO(tc.refNumber, tc.clientId, currentBalance.amount, "Deposit Successful")))
            }

          }
          case e: JsError => {
            Future{
              BadRequest(Json.toJson(BadRequestDTO("Input Json was malformed")))
            }
          }
        }
  }

  def withdraw = Action.async {
    request =>
      implicit val successWriter = Json.writes[SuccessfulWithdrawDTO]
      implicit val insufficientFundsWriter = Json.writes[InsufficientFundsDTO]
      implicit val badRequestWriter = Json.writes[BadRequestDTO]

      request.body.asJson.get.validate[WithdrawDTO] match {
        case s: JsSuccess[WithdrawDTO] => {
          val withdraw = s.value
          val clientID = withdraw.clientId

          val result = for{
            withdraw <- bankAccountActor ? WithdrawMoney(withdraw.amount, clientID)
            currentBalance <- bankAccountActor ? GetCurrentBalance(clientID)
          }
            yield{
              val balance = currentBalance.asInstanceOf[Balance]
              withdraw match {
                case m:InsufficientFunds =>
                  Json.toJson(InsufficientFundsDTO(m.clientId, m.amountDesired, balance.amount, s"InsufficientFunds for ${m.amountDesired} only have ${balance.amount}"))
                case m: TransactionSuccessful =>
                  Json.toJson(SuccessfulWithdrawDTO(m.refNumber, m.clientId, balance.amount, "Withdraw Successful"))
              }
            }
            result.map{msg =>Ok(msg)}
        }
        case e: JsError => {
          Future{
            BadRequest(Json.toJson(BadRequestDTO("Input Json was malformed")))
          }
        }
      }
  }

  def listTransactions(clientId: String, number: Int) = Action.async {
    implicit val statementWrite = Json.writes[Statement]
      val result = bankAccountActor ? ListMostRecentTransactions(number, clientId)
      result.map{statement => Ok(Json.toJson(statement.asInstanceOf[Statement]))}
  }

  def currentBalance(clientId: String) = Action.async {
    implicit val balanceWrites = Json.writes[Balance]
    val result = bankAccountActor ? GetCurrentBalance(clientId)
    result.map{balance =>
      Ok(Json.toJson(balance.asInstanceOf[Balance]))
    }
  }
}
