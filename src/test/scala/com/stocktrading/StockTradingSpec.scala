package com.stocktrading

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import StockTrading._

class StockTradingSpec extends WordSpec with Matchers with MockFactory {
  "A StockTrading" when {
    val accounts = parseAccounts()
    "orders.txt" must {
      val orders = parseOrders()
      val updated = process(orders, accounts)
      "parse accounts data" in {
        accounts.size shouldBe 9
      }
      "parse orders data" in {
        orders.size shouldBe 505
      }
      "process all data" in {
        updated.size shouldBe 9
      }
      "update balance" in {
        accounts.map(_._2.usdBalance).sum shouldBe updated.map(_._2.usdBalance).sum
      }
      "update positionA" in {
        accounts.map(_._2.positions(PositionA)).sum shouldBe updated.map(_._2.positions(PositionA)).sum
      }
      "update positionB" in {
        accounts.map(_._2.positions(PositionB)).sum shouldBe updated.map(_._2.positions(PositionB)).sum
      }
      "update positionC" in {
        accounts.map(_._2.positions(PositionC)).sum shouldBe updated.map(_._2.positions(PositionC)).sum
      }
      "update positionD" in {
        accounts.map(_._2.positions(PositionD)).sum shouldBe updated.map(_._2.positions(PositionD)).sum
      }
    }
    "orders-0001.txt" must {
      val orders = parseOrders("/orders-0001.txt")
      val updated = process(orders, accounts)
      "update positionC" in {
        updated("C8").usdBalance shouldBe 6860
        updated("C8").positions(PositionC) shouldBe accounts("C8").positions(PositionC) + 8
        updated("C4").usdBalance shouldBe 740
        updated("C4").positions(PositionC) shouldBe accounts("C4").positions(PositionC) - 12
        updated("C1").usdBalance shouldBe 952
        updated("C1").positions(PositionC) shouldBe accounts("C1").positions(PositionC) + 4
      }
      "update positionA" in {
        updated("C8").usdBalance shouldBe 6860
        updated("C8").positions(PositionA) shouldBe accounts("C8").positions(PositionA) + 12
        updated("C1").usdBalance shouldBe 952
        updated("C1").positions(PositionA) shouldBe accounts("C1").positions(PositionA) - 8
        updated("C2").usdBalance shouldBe 4358
        updated("C2").positions(PositionA) shouldBe accounts("C2").positions(PositionA) - 4
      }
      "update balance" in {
        accounts.map(_._2.usdBalance).sum shouldBe updated.map(_._2.usdBalance).sum
      }
    }
  }
}