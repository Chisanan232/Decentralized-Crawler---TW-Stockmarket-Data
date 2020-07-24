package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.CrawlerSoldier
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._

import scala.collection.mutable.ListBuffer

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class CrawlerPaladin extends Actor with ActorLogging {

  /** The logic of actors state Map value is "Does it could receive next task?" **/
  var actorsState: Map[String, Boolean] = Map[String, Boolean]()
  var leftAPIMap: Map[String, ListBuffer[String]] = Map[String, ListBuffer[String]]()

  private val check = new CheckMechanism

  private def initActorsState(actorName: String): Map[String, Boolean] = {
    actorsState += (AkkaConfig.CrawlerDepartment.SoldierName -> true)
    actorsState
  }

  private def recordHistory(key: String, date: String): Map[String, ListBuffer[String]] = {
    /*
    Update the datetime data after examiner soldiers get the data every time
     */
    val newDateArray = this.leftAPIMap(key).filter(!_.equals(date))
    leftAPIMap += (key -> newDateArray)
    leftAPIMap
  }

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
      for (crawlerID <- 0.until(AkkaConfig.CrawlerNumber)) {
        val actorName = AkkaConfig.CrawlerDepartment.SoldierName + crawlerID.toString
        crawlerSoldiers(crawlerID) = context.actorOf(Props[CrawlerSoldier], actorName)
        this.initActorsState(actorName)
      }
      crawlerSoldiers.foreach(crawlerSoldierRef => {
        val soldier = context.actorSelection(crawlerSoldierRef.path)
        soldier ! ReadyOnStandBy
        actorsState += (crawlerSoldierRef.path.name -> false)
      })


    /** Check actor status and distribute tasks to soldiers **/
    case GotAPI(content, api) =>
      log.info("\uD83D\uDCEC Got API.")
      for (crawlerID <- 0.until(AkkaConfig.CrawlerNumber)) {
        val actorName = AkkaConfig.CrawlerDepartment.SoldierName + crawlerID.toString
        if (actorsState(actorName).equals(true)) {
          // Send task to it
          context.actorSelection(actorName) ! TargetAPI("\uD83D\uDCE9 Here is API info!", api)
        } else {
          // Save task to variable
          val latestAPIs = leftAPIMap(actorName) += api
          leftAPIMap += (actorName -> latestAPIs)
        }
      }


    case FinishCurrentJob(content, actor) =>
      log.info(s"$content from AKKA actor $actor")
      actorsState += (actor -> true)

  }

}
