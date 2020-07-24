package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data.{APIDate, DataSource}
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataProducerManagement
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.SniffDataSoldier

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class SniffDataPaladin extends Actor with ActorLogging {

  val dataRefreshTimeout = 3
  var dataRefresh: Boolean = false
  var previousDataRefresh: Boolean = false
  var dataRefreshTime: Int = 0
  var previousDataRefreshTime: Int = 0

  val ad = new APIDate
  var allAPIData: Array[String] = ad.targetDateRange().toArray

  val ds = new DataSource
  val stockSymbolList: List[Any] = ds.stockSymbolData()

  var checkMap: Map[String, Array[String]] = Map[String, Array[String]]()
  for (symbol <- stockSymbolList) checkMap += (symbol.toString -> allAPIData)

  private def recordHistory(key: String, date: String): Map[String, Array[String]] = {
    /*
    Update the datetime data after examiner soldiers get the data every time
     */
    val newDateArray = this.checkMap(key).filter(!_.equals(date))
    this.checkMap += (key -> newDateArray)
    this.checkMap
  }

  override def receive: Receive = {

    case CallDataSnifferPaladin =>
      log.info("\uD83E\uDD18 I Receive task!")
      val searchLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $searchLeaderPath"
      sender() ! msg


    case NeedCrawlerCondition =>
      log.info("\uD83D\uDC40 Checking crawler conditions ...")

      val sniffSoldierRef = context.actorOf(Props[SniffDataSoldier], AkkaConfig.DataAnalyserDepartment.SniffSoldierName)
      val sniffSoldier = context.actorSelection(sniffSoldierRef.path)
      sniffSoldier ! NeedAPIInfo

  }

}
