package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.CrawlerSoldier
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class CrawlerPaladin extends Actor with ActorLogging {

  private val check = new CheckMechanism

  override def receive: Receive = {

    /** Initial CrawlerPaladin AKKA actor **/
    case CallCrawlerPaladin =>
      log.info("I Receive task!")
      val crawlerLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $crawlerLeaderPath"
      sender() ! msg


    /** Build up crawler soldiers (in other words, they're crawler threads) **/
    case AwaitDataAndCrawl =>
      log.info("\uD83D\uDCAA Okay, come on, my greatest crawler soldiers. Go work!")

      val crawlerSoldiers = new Array[ActorRef](AkkaConfig.CrawlerNumber)
      for (crawlerID <- 0.until(AkkaConfig.CrawlerNumber)) crawlerSoldiers(crawlerID) = context.actorOf(Props[CrawlerSoldier], AkkaConfig.CrawlerDepartment.SoldierName + crawlerID.toString)
      crawlerSoldiers.foreach(crawlerSoldierRef => {
        val soldier = context.actorSelection(crawlerSoldierRef.path)
        soldier ! ReadyOnStandBy
      })

  }

}
