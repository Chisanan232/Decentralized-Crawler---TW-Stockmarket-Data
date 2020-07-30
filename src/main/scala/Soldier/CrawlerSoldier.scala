package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Success, Failure}

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout


class CrawlerSoldier extends Actor with ActorLogging {

  final val king = AkkaConfig.KingName
  final val paladin = AkkaConfig.DataAnalyserDepartment.DataSaverPaladinName

  private val check = new CheckMechanism

  override def receive: Receive = {

    case ReadyOnStandBy =>
      log.info("Roger that!")
      log.info("Waiting for data ....")


    case TargetAPI(content, api) =>
      log.info("\uD83D\uDCEC Receive API info.")
      log.debug("**************** Debug for consumer in my system *********************")
      log.debug("api: " + api)
      log.debug("**************** Overwrite failure *********************")

      // Crawl data
      val te = new TasksExecutor
      val (symbol, date, crawlResult) = te.runCode(api)
      log.info(s"📄 Crawl data and it's $crawlResult")

      // Save data into database Cassandra if it's a great data.
      if (crawlResult.isEmpty.equals(false)) {

        implicit val timeout: Timeout = Timeout(5.seconds)
        context.system.actorSelection(s"user/$king/$paladin").resolveOne().onComplete{
          case Success(actor) =>
            log.info("\uD83C\uDF89 Save data into database Cassandra!")
            actor ! SaveData("\uD83D\uDCC4 Here is target data!", StockMarket, crawlResult)
            context.parent ! FinishCurrentJob("✅ FINISH", self.path.name.toString)
          case Failure(exception) =>
            log.info("\uD83D\uDEAB Cannot save data into database because cannot find the AKKA actor that is data saver.")
            context.parent ! FinishCurrentJob("❎ FINISH", self.path.name.toString)
        }

      } else {
        log.info("\uD83C\uDF1A Data has some problem ....")
        context.parent ! FinishCurrentJob("❎ FINISH", self.path.name.toString)
      }

  }

}
