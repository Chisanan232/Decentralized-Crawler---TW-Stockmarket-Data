package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.SniffDataSoldier

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class SniffDataPaladin extends Actor with ActorLogging {

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
