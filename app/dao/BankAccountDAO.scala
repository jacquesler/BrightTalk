package dao

import java.time.LocalDateTime
import java.util.UUID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Transaction(refNumber: UUID, amount: BigDecimal, datetime: LocalDateTime)
case class Transactions(clientId: String, transactions: List[Transaction])
case class TransactionComplete(clientId: String, refNumber: UUID)
case class TransactionBalance(clientId: String, amount: BigDecimal)

trait BankAccountDAO {

  def deposit(amount: BigDecimal, clientId: String): Future[TransactionComplete]
  def withdraw(amount: BigDecimal, clientId: String): Future[TransactionComplete]
  def listTransactions(number: Int, clientId: String): Future[Transactions]
  def getBalance(clientId: String): Future[TransactionBalance]
  def isUser(clientId: String): Boolean
}

case class MockBankAccountDAO() extends BankAccountDAO{
  var map: Map[String, List[Transaction]] = Map(
    "jacquesja" -> List(Transaction(UUID.fromString("d4e3db8f-2fc2-4e95-98bf-464f9cf86a28"), BigDecimal(500), LocalDateTime.now())),
    "johndoe" -> List(Transaction(UUID.fromString("d4e3db8f-2fc2-4e95-98bf-464f9cf86a28"), BigDecimal(200), LocalDateTime.now()))
  )

  override def isUser(clientId: String): Boolean = {
    map.exists{user => user._1.equalsIgnoreCase(clientId)}
  }

  override def deposit(amount: BigDecimal, clientId: String): Future[TransactionComplete] = Future{
    val refNumber = UUID.randomUUID()
    val newTransactionList = Transaction(refNumber, amount, LocalDateTime.now()) +: map.getOrElse(clientId, List())
    map += (clientId -> newTransactionList)
    TransactionComplete(clientId, refNumber)
  }

  override def listTransactions(number: Int, clientId: String): Future[Transactions] = Future{
    val lines = map.getOrElse(clientId, List()).take(number)
    Transactions(clientId, lines)
  }

  override def withdraw(amount: BigDecimal, clientId: String): Future[TransactionComplete] = Future{
    val refNumber = UUID.randomUUID()
    val newTransactionList = Transaction(refNumber, -amount, LocalDateTime.now()) +: map.getOrElse(clientId, List())
    map += (clientId -> newTransactionList)
    TransactionComplete(clientId, refNumber)
  }

  override def getBalance(clientId: String): Future[TransactionBalance] = Future {
    val transactions = map.getOrElse(clientId, List())
    val balance = transactions.foldLeft(BigDecimal(0))((r, c) => r + c.amount)
    TransactionBalance(clientId, balance)
  }
}