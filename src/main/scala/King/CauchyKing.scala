package Taiwan_stock_market_crawler_Cauchy.src.main.scala.King

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin.{DataProducerPaladin, SniffDataPaladin, CrawlerPaladin, DataSaverPaladin}
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data.{DataSource, APIDate}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.Future


class CauchyKing extends Actor with ActorLogging {

  import context.dispatcher

  private val check = new CheckMechanism
  private val preData = new DataSource
  private val preDataDatetime = new APIDate

  var totalTasksNum: BigInt = _
  var currentDoneTaskNum = 0

  var StockIDs: List[Any] = _
  var DateTimes: List[String] = _

  final private val pp: String = AkkaConfig.DataAnalyserDepartment.ProducerPaladinName
  final private val sniffp: String = AkkaConfig.DataAnalyserDepartment.SniffDateSoldierName
  final private val cp: String = AkkaConfig.CrawlerDepartment.PaladinName
  final private val saverp: String = AkkaConfig.CrawlerDepartment.DataSaverName

  /*** Initial AKKA actor object **/
  private final def getActorRef(actorName: String): ActorRef = {
    actorName match {
      case this.pp => context.actorOf(Props[DataProducerPaladin], this.pp)
      case this.sniffp => context.actorOf(Props[SniffDataPaladin], this.sniffp)
      case this.cp => context.actorOf(Props[CrawlerPaladin], this.cp)
      case this.saverp => context.actorOf(Props[DataSaverPaladin], this.saverp)
    }
  }

  /** Send request signal to Paladin **/
  private final def sendCallMsg(actor: ActorSelection, actorName: String)(implicit timeout: Timeout): Future[Any] = {
    actorName match {
      case this.pp => actor ? CallDataProducerPaladin
      case this.sniffp => actor ? CallDataSnifferPaladin
      case this.cp => actor ? CallCrawlerPaladin
      case this.saverp => actor ? CallDataSaverPaladin
    }
  }

  /** Send Task content to Paladin **/
  private final def sendTaskMsg(actor: ActorSelection, actorName: String): Unit = {
    actorName match {
      case this.pp => actor ! GenerateAPI("\uD83D\uDC40 Here are pre-data all we need.", totalTasksNum, StockIDs, DateTimes)
      case this.sniffp => actor ! NeedCrawlerCondition
      case this.cp => actor ! AwaitDataAndCrawl("Here is the total tasks number it has", totalTasksNum)
      case this.saverp => log.info("Nothing need to do")
    }
  }

  /** The main task content which is running in King Actor **/
  private final def runTask(actorName: String): Unit = {
    implicit val timeout: Timeout = Timeout(10.seconds)
    val PaladinRef = this.getActorRef(actorName)
    val Paladin = context.actorSelection(PaladinRef.path)
    val Resp = this.sendCallMsg(Paladin, actorName)
    val Checksum = this.check.waitAnswer(Resp, PaladinRef.path.toString)
    if (Checksum.equals(true)) this.sendTaskMsg(Paladin, actorName)
  }


  override def preStart(): Unit = {
    log.info("Initial pre-data we need.")

    // Load data into King Actor.
    totalTasksNum = this.preData.dataNumber()
    StockIDs = this.preData.stockSymbolData()
    DateTimes = this.preDataDatetime.targetDateRange()

    this.preData.closeSpark()

    super.preStart()
  }


  override def receive: Receive = {

    case DataAim =>
      log.info("All right, begin to work, guys!")
      log.info("Hello, premiers, I need your help.")

      this.runTask(this.pp)
      this.runTask(this.sniffp)
      this.runTask(this.cp)
      this.runTask(this.saverp)


    case TotalDataNum(content, total) =>
      log.info(s"All data amount is $total")
      totalTasksNum = total


    case SaveFinish =>
      currentDoneTaskNum += 1
      log.info("\uD83C\uDFC1 Finish 1 task!")
      if (currentDoneTaskNum.equals(totalTasksNum)) {
        // Finish all tasks and end the AKKA system
        log.info("\uD83C\uDDF9\uD83C\uDDFC\uD83C\uDFC6\uD83C\uDF8A\uD83C\uDF89 Finish all tasks and end the AKKA system.")
        context.system.terminate()
      }

  }

}
