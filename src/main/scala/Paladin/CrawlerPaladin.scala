package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.CrawlerSoldier
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.{break, breakable}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class CrawlerPaladin extends Actor with ActorLogging {

  /** The logic of actors state Map value is "Does it could receive next task?" **/
  var currentReceiveNum: BigInt = _      // The total API amount Paladin got currently
  var allNum: BigInt = _                 // The total API amount
  var actorsState: Map[String, Boolean] = Map[String, Boolean]()      // Record every soldier actors crawl-state
  var leftAPIMap: Map[String, ListBuffer[String]] = Map[String, ListBuffer[String]]()   // Record all left APIs

  /** Initial actors states **/
  private def initActorsState(actorName: String): Map[String, Boolean] = {
    actorsState += (AkkaConfig.CrawlerDepartment.SoldierName -> true)
    actorsState
  }

  /** Update the actor state data **/
  private def recordHistory(key: String, date: String): Map[String, ListBuffer[String]] = {
    /*
    Update the datetime data after examiner soldiers get the data every time
     */
    val newDateArray = leftAPIMap(key).filter(!_.equals(date))
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
    case AwaitDataAndCrawl(content, allTaskNum) =>
      allNum = allTaskNum
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
      currentReceiveNum += 1
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
      if (currentReceiveNum.equals(allNum)) {
        self ! ClearData
      }


    /** Handle the all of left APIs **/
    case ClearData =>
      log.info("\uD83E\uDDF9 Clean the data if it left.")
      breakable {
        while (true) {
          var clearAll = true
          for ((actor, leftAPI) <- leftAPIMap) {
            log.info(s"Checking AKKA actor $actor")
            if (leftAPI.nonEmpty) {
              // Send task to actor
              clearAll = false
              if (actorsState(actor).equals(true)) {
                context.actorSelection(actor) ! TargetAPI("\uD83D\uDCE9 Here is API info!", leftAPI.apply(1))
                this.recordHistory(actor, leftAPI.apply(1))
              }
            }

            if (clearAll.equals(true)) {
              // Break and end this actor
              log.info("\uD83E\uDDFC Finish to clean all data.")
              break()
            }

            // Checking this every 10 seconds.
            Thread.sleep(10000)
          }
        }
      }


    /** Receive Soldiers done signal and change the actor state **/
    case FinishCurrentJob(content, actor) =>
      log.info(s"$content from AKKA actor $actor")
      actorsState += (actor -> true)

  }
}
