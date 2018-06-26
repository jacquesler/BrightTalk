package commands

import java.util.UUID

sealed trait AccountActivity
case class DepositMoney(amount: BigDecimal, clientId: String)
case class WithdrawMoney(amount: BigDecimal, clientId: String)
case class GetCurrentBalance(clientId: String)
case class ListMostRecentTransactions(nr: Int, clientId: String)
case class CurrentBalance(amount: BigDecimal, clientId: String)