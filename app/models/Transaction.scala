package models

import java.time.LocalDateTime
import java.util.UUID

case class Deposit(clientId: String, amount: BigDecimal)
case class Withdraw(clientId: String, amount: BigDecimal)