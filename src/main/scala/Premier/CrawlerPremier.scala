package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Premier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.CrawlerSoldier

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class CrawlerPremier extends Actor with ActorLogging {

  def receive: Receive = {

    case CallCrawlerPremier =>
      val crawlPremierPath = context.self.path
      val msg = s"I'm ready! I'm $crawlPremierPath"
      log.info(msg)
      sender() ! msg


    case AwaitDataAndCrawl =>
      log.info("Start to get all conditions which be needed by crawler.")

      // 1. Call Data Department guys that I need some conditions to crawl target data.
      // Here code for consumer
//       val consumerPaladinRef = context.actorOf(Props[DataConsumerPaladin], AkkaConfig.DataAnalyserDepartment.ConsumerPaladinName)
//      AkkaConfig.DataAnalyserDepartment.ConsumerPaladinPath = consumerPaladinRef.path.toString
//      consumerPaladin ! NeedCrawlerCondition

      // Here code for producer leader
//      val producerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath)
//      log.info("Does I has Producer Leader mailbox where I could send Generation API notification to him?")
//      val producerPaladinPath = AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath
      /***
       * Question:
       * Here is the running result :
       * [INFO] [06/20/2020 20:14:38.833] [CauchySystem-akka.actor.default-dispatcher-3] [akka://CauchySystem/user/King/CrawlerPremier] Does I has Producer Leader mailbox where I could send Generation API notification to him?
       * [INFO] [06/20/2020 20:14:38.834] [CauchySystem-akka.actor.default-dispatcher-3] [akka://CauchySystem/user/King/CrawlerPremier] producerPaladin path is
       * [INFO] [06/20/2020 20:14:38.834] [CauchySystem-akka.actor.default-dispatcher-5] [akka://CauchySystem/user/King] Thank you my man, Data Premier.
       * That means that you doesn't have the Path of Producer leader. In the other word, you doesn't have the target mailbox lead to you couldn't send email ot him.
       *
       * Solution:
       * You should wait for the mailbox exist.
       */
      val check = new CheckMechanism

      if (check.actorPathExists(AkkaConfig.DataAnalyserDepartment.ConsumerPaladinName).equals(true)) {
        val consumerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ConsumerPaladinPath)
        consumerPaladin ! NeedCrawlerCondition
      } else {
        // Raise an error about cannot find the target path
      }

//      if (check.actorPathExists(AkkaConfig.DataAnalyserDepartment.ProducerPaladinName).equals(true)) {
//        val producerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath)
//        producerPaladin ! GenerateAPI("", context.self.path.toString)
//      } else {
//        // Raise an error about cannot find the target path
//      }

      // 2. Build up my man Crawler Soldier.
      val crawlerSoldiers = new Array[ActorRef](AkkaConfig.CrawlerNumber)
      for (soldierID <- 0.until(AkkaConfig.CrawlerNumber)) crawlerSoldiers(soldierID) = context.actorOf(Props[CrawlerSoldier], AkkaConfig.CrawlerDepartment.CrawlSoldierName + s"$soldierID")
      crawlerSoldiers.foreach(soldierRef => {
        val soldierPaths = AkkaConfig.CrawlerDepartment.CrawlSoldierPaths
        soldierPaths(soldierRef.path.name.toString.takeRight(1).toInt) = soldierRef.path.toString
        val soldier = context.actorSelection(soldierRef.path)
        soldier ! ReadyOnStandBy
      })

  }

}
