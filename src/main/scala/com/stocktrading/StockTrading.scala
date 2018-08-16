package com.stocktrading

import java.io._
import scala.io.Source

/**
  * Операция покупки или продажи ценной бумаги
  */
abstract class Action(val value: String)
case object Sell extends Action("s")
case object Buy extends Action("b")

object Action {
  def apply(v: String): Action = v match {
    case Sell.value => Sell
    case Buy.value => Buy
  }
}

/**
  * Все возможные варианты позиций по ценным бумагам
  */
abstract class Position(val value: String)
case object PositionA extends Position("A")
case object PositionB extends Position("B")
case object PositionC extends Position("C")
case object PositionD extends Position("D")

object Position {
  def apply(v: String): Position = v match {
    case PositionA.value => PositionA
    case PositionB.value => PositionB
    case PositionC.value => PositionC
    case PositionD.value => PositionD
  }
}

/**
  * Счет клиента, содержащий баланс и позиции по ценным бумагам
  */
case class Account(clientId: String, usdBalance: Int, positions: Map[Position, Int]) {
  def buy(position: Position, price: Int, amount: Int): Account = copy(
    usdBalance = this.usdBalance - price * amount,
    positions = positions.updated(position, positions(position) + amount)
  )
  def sell(position: Position, price: Int, amount: Int): Account = copy(
    usdBalance = this.usdBalance + price * amount,
    positions = positions.updated(position, positions(position) - amount)
  )
}

/**
  * Счет клиента
  */
case class Order(clientId: String, action: Action, position: Position, price: Int, amount: Int) extends Serializable  {
  require(price > 0 && amount > 0)
}

object StockTrading {
  /**
    * Парсим файл по строкам, мапим каждую строку в case class Account.
    * Получаем Map(клиент -> счет).
    */
  def parseAccounts(path: String = "/clients.txt"): Map[String, Account] = {
    Source.fromURL(getClass.getResource(path)).getLines().map(line => {
      val arr = line.split("\t")
      val clientId = arr(0)
      clientId -> Account(
        clientId = clientId,
        usdBalance = arr(1).toInt,
        positions = Map(
          PositionA -> arr(2).toInt,
          PositionB -> arr(3).toInt,
          PositionC -> arr(4).toInt,
          PositionD -> arr(5).toInt
        )
      )
    }).toMap
  }

  /**
    * Парсим файл по строкам, мапим каждую строку в case class Order.
    * Map(клиент -> заявка)
    */
  def parseOrders(path: String = "/orders.txt"): Seq[Order] = {
    Source.fromURL(getClass.getResource(path)).getLines().map(line => {
      val arr = line.split("\t")
      val clientId = arr(0)
      Order(
        clientId,
        Action(arr(1)),
        Position(arr(2)),
        arr(3).toInt,
        arr(4).toInt
      )
    }).toSeq
  }

  def dropFirstMatch[A](ls: Seq[A], value: A): Seq[A] = {
    val index = ls.indexOf(value)  //index is -1 if there is no match
    if (index < 0) ls
    else if (index == 0) ls.tail
    else {
      // splitAt keeps the matching element in the second group
      val (a, b) = ls.splitAt(index)
      a ++ b.tail
    }
  }

  def process(orders: Seq[Order], accounts: Map[String, Account]): Map[String, Account] = {
    // разбиваем заявки на две коллекции (продажа, покупка)
    val (buyOrders, sellOrders) = orders.partition(_.action == Buy)
    // математическая свертка по заявкам покупки
    val (processed, _) = ((accounts, sellOrders) /: buyOrders)({
      case ((account, x1SellOrders), buyOrder) =>
        val x2SellOrders = x1SellOrders
          .filter(_.clientId != buyOrder.clientId) // ситуация продажи и покупки самому себе
          .find(order => order.amount == buyOrder.amount && order.price == buyOrder.price)

        if (x2SellOrders.nonEmpty) {
          val currentSellOrder = x2SellOrders.get
          val buyer = buyOrder.clientId
          val seller = currentSellOrder.clientId
          val position = buyOrder.position
          val price = buyOrder.price
          val amount = buyOrder.amount
          val updatedAccount: Map[String, Account] = account
            .updated(buyer, account(buyer).buy(position, price, amount))
            .updated(seller, account(seller).sell(position, price, amount))
          (updatedAccount, dropFirstMatch(x1SellOrders, currentSellOrder))
        } else (account, x1SellOrders)
    })
    processed
  }

  def main(args: Array[String]): Unit = {
    val orders = parseOrders()
    val accounts = parseAccounts()
    val updated = process(orders, accounts).toSeq.sortBy(_._1)
    val data = updated.map({
      case (clientId, Account(_, balance, positions)) =>
        val r = new StringBuilder
        r ++= clientId + "\t"
        r ++= balance.toString
        for (i <- positions.toSeq.map(_._2)) r ++= "\t" + i
        r
    })
    data foreach println
    val file = "target/result.txt"
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
    for (x <- data) writer.write(x + "\n")
    writer.close()
  }
}