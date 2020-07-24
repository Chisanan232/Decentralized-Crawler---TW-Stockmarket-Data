package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.DataBaseOps

import akka.actor.{Actor, ActorLogging}


class DataSaverPaladin extends Actor with ActorLogging {

  private val dbOpts = new DataBaseOps

  override def receive: Receive = {

    /** Initial CrawlerPaladin AKKA actor **/
    case CallDataSaverPaladin =>
      log.info("I Receive task!")
      val dataSaverLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $dataSaverLeaderPath"
      sender() ! msg


    case SaveData(content, data) =>
      log.info("")

  }

}
