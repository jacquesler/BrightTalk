package models

import java.time.LocalDateTime
import java.util.UUID

case class DepositDTO(clientId: String, amount: BigDecimal)
case class WithdrawDTO(clientId: String, amount: BigDecimal)
case class BadRequestDTO(reason: String)
case class SuccessfulWithdrawDTO(refNumber: UUID, clientId: String, currentBalance: BigDecimal, reason: String)
case class SuccessfulDepositDTO(refNumber: UUID, clientId: String, currentBalance: BigDecimal, reason: String)
case class InsufficientFundsDTO(clientId: String, amountDesired: BigDecimal, currentBalance: BigDecimal, reason: String)