package controllers.jsonTransformers

import java.time.format.DateTimeFormatter
import java.util.UUID

import actors.BankAccountActor
import actors.BankAccountActor.{Balance, Statement, StatementLineItem}
import models._
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.json._
import play.api.libs.functional.syntax._

object BankAccountJsonTransformer {

  implicit val depositReads: Reads[DepositDTO] = (
    (JsPath \ "clientId").read[String] and
      (JsPath \ "amount").read[BigDecimal]
    )(DepositDTO.apply _)

  implicit val withdrawReads: Reads[WithdrawDTO] = (
      (JsPath \ "clientId").read[String] and
      (JsPath \ "amount").read[BigDecimal]
    )(WithdrawDTO.apply _)


  implicit val balanceWrites = new Writes[Balance] {
    def writes(balance: Balance) = Json.obj(
      "clientId" -> balance.clientId,
      "amount" -> balance.amount
    )
  }

  val pattern = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm")
  implicit val statementLineItemWrites = new Writes[StatementLineItem] {
    def writes(lineItem: StatementLineItem) = Json.obj(
      "Date" -> lineItem.datetime.format(pattern),
      "Reference" -> lineItem.refNumber,
      "Amount" -> lineItem.amount
    )
  }

  implicit val statementWrites = new Writes[Statement] {
    def writes(statement: Statement) = Json.obj(
      "ClientId" -> statement.clientId,
      "LineItems" -> statement.statementLineItems
    )
  }

  implicit val successfulWithdrawDtoWrites = new Writes[SuccessfulWithdrawDTO] {
    def writes(successfulWithdrawDTO: SuccessfulWithdrawDTO) = Json.obj(
      "ClientId" -> successfulWithdrawDTO.clientId,
      "Reference" -> successfulWithdrawDTO.refNumber,
      "Balance" -> successfulWithdrawDTO.currentBalance,
      "Action" -> successfulWithdrawDTO.reason
    )
  }

  implicit val successfulDepositDtoWrites = new Writes[SuccessfulDepositDTO] {
    def writes(successfulDepositDTO: SuccessfulDepositDTO) = Json.obj(
      "ClientId" -> successfulDepositDTO.clientId,
      "Reference" -> successfulDepositDTO.refNumber,
      "Balance" -> successfulDepositDTO.currentBalance,
      "Action" -> successfulDepositDTO.reason
    )
  }

  implicit val insufficientFundsDtoWrites = new Writes[InsufficientFundsDTO] {
    def writes(insufficientFundsDTO: InsufficientFundsDTO) = Json.obj(
      "ClientId" -> insufficientFundsDTO.clientId,
      "Balance" -> insufficientFundsDTO.currentBalance,
      "Action" -> insufficientFundsDTO.reason
    )
  }

  implicit val badRequestDtoWrites = new Writes[BadRequestDTO] {
    def writes(badrequest: BadRequestDTO) = Json.obj(
      "Error" -> "Error 101",
      "Reason" -> badrequest.reason
    )
  }


}