package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._

import akka.actor.{Actor, ActorLogging, Props}


class CrawlerSoldier extends Actor with ActorLogging {

  val check = new CheckMechanism

  def receive: Receive = {

    case ReadyOnStandBy =>
      log.info("Roger that!")
      log.info("Waiting for data ....")


    case TargetAPI(content, api) =>
      log.info("Receive API info.")
      println("**************** Debug for consumer in my system *********************")
      println("api: " + api)
      println("**************** Overwrite failure *********************")
      val te = new TasksExecutor
      val (symbol, date, crawlResult) = te.runCode(api)
      log.info(s"Crawl data and it's $crawlResult")
      // Save data into database Cassandra if it's a great data.
      if (crawlResult.isEmpty.equals(false)) {
        val producerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath)
        producerPaladin ! FinishAPI("Here is the datetime mapping a company stock symbol which be finish to crawl.", symbol, date)
        if (this.check.actorPathExists(AkkaConfig.DataAnalyserDepartment.ProducerDateSoldierPath).equals(true)) {
          val dateProducer = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerDateSoldierPath)
          dateProducer ! ProduceDate("Here is the datetime data crawler soldier need.", symbol, date)
        }
        log.info("Save data into database Cassandra!")
      } else {
        log.info("Data has some problem ....")
      }

  }

}
