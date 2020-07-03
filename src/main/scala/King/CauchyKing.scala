package Taiwan_stock_market_crawler_Cauchy.src.main.scala.King

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Premier.{CrawlerPremier, DataPremier}

import scala.concurrent.duration._

import akka.actor.{Actor, Props, ActorLogging}
import akka.util.Timeout
import akka.pattern.ask


class CauchyKing extends Actor with ActorLogging {

  val check = new CheckMechanism

  var totalTasksNum = 0
  var currentDoneTaskNum = 0

  def receive: Receive = {

    case DataAim =>
      log.info("All right, begin to work, guys!")
      log.info("Hello, premiers, I need your help.")
      AkkaConfig.KingPath = context.self.path.toString

      implicit val timeout = Timeout(10.seconds)

      val crawlerPremierRef = context.actorOf(Props[CrawlerPremier], AkkaConfig.CrawlerDepartment.CrawlPremierName)
      AkkaConfig.CrawlerDepartment.PremierPath = crawlerPremierRef.path.toString
      val crawlerPremierActor = context.actorSelection(crawlerPremierRef.path)
      val crawlResp = crawlerPremierActor ? CallCrawlerPremier
      val crawlChecksum = this.check.waitAnswer(crawlResp, crawlerPremierRef.path.toString)
      if (crawlChecksum.equals(true)) {
        log.info("Thank you my man, Crawler Premier.")
        crawlerPremierActor ! AwaitDataAndCrawl
      }

      val dataPremierRef = context.actorOf(Props[DataPremier], AkkaConfig.DataAnalyserDepartment.DataPremierName)
      AkkaConfig.DataAnalyserDepartment.PremierPath = dataPremierRef.path.toString
      val dataPremierActor = context.actorSelection(dataPremierRef.path)
      val dataResp = dataPremierActor ? CallDataPremier
      val dataChecksum = this.check.waitAnswer(dataResp, dataPremierRef.path.toString)
      if (dataChecksum.equals(true)) {
        log.info("Thank you my man, Data Premier.")
        dataPremierActor ! MayIUseData
      }


    case TotalDataNum(content, total) =>
      log.info(s"All data amount is $total")
      this.totalTasksNum = total


    case FinishTask =>
      log.info("")

  }

}
