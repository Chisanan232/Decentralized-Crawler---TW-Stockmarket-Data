package Taiwan_stock_market_crawler_Cauchy.src.main.scala.King

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin.{DataProducerPaladin, SniffDataPaladin, CrawlerPaladin}
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data.{DataSource, APIDate}

import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future


class CauchyKing extends Actor with ActorLogging {

  import context.dispatcher

  private val check = new CheckMechanism
  private val preData = new DataSource
  private val preDataDatetime = new APIDate

  var totalTasksNum: Float = _
  var currentDoneTaskNum = 0

  var StockIDs: List[Any] = _
  var DataTimes: List[String] = _

  final private val pp: String = AkkaConfig.DataAnalyserDepartment.ProducerPaladinName
  final private val sp: String = AkkaConfig.DataAnalyserDepartment.SearchDateSoldierName
  final private val cp: String = AkkaConfig.CrawlerDepartment.PaladinName

  /*** Initial AKKA actor object **/
  private final def getActorRef(actorName: String): ActorRef = {
    actorName match {
      case this.pp => context.actorOf(Props[DataProducerPaladin], this.pp)
      case this.sp => context.actorOf(Props[SniffDataPaladin], this.sp)
      case this.cp => context.actorOf(Props[CrawlerPaladin], this.cp)
    }
  }

  /** Send request signal to Paladin **/
  private final def sendCallMsg(actor: ActorSelection, actorName: String)(implicit timeout: Timeout): Future[Any] = {
    actorName match {
      case this.pp => actor ? CallDataProducerPaladin
      case this.sp => actor ? CallDataSnifferPaladin
      case this.cp => actor ? CallCrawlerPaladin
    }
  }

  /** Send Task content to Paladin **/
  private final def sendTaskMsg(actor: ActorSelection, actorName: String): Unit = {
    actorName match {
      case this.pp => actor ! MayIUseData
      case this.sp => actor ! NeedCrawlerCondition
      case this.cp => actor ! AwaitDataAndCrawl
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
    DataTimes = this.preDataDatetime.targetDateRange()

    super.preStart()
  }


  override def receive: Receive = {

    case DataAim =>
      log.info("All right, begin to work, guys!")
      log.info("Hello, premiers, I need your help.")

      this.runTask(this.pp)
      this.runTask(this.sp)
      this.runTask(this.cp)

//      implicit val timeout: Timeout = Timeout(10.seconds)
//
//      val crawlerPremierRef = context.actorOf(Props[CrawlerPremier], AkkaConfig.CrawlerDepartment.CrawlPremierName)
//      AkkaConfig.CrawlerDepartment.PremierPath = crawlerPremierRef.path.toString
//      val crawlerPremierActor = context.actorSelection(crawlerPremierRef.path)
//      val crawlResp = crawlerPremierActor ? CallCrawlerPremier
//      val crawlChecksum = this.check.waitAnswer(crawlResp, crawlerPremierRef.path.toString)
//      if (crawlChecksum.equals(true)) {
//        log.info("Thank you my man, Crawler Premier.")
//        crawlerPremierActor ! AwaitDataAndCrawl
//      }
//
//      val dataPremierRef = context.actorOf(Props[DataPremier], AkkaConfig.DataAnalyserDepartment.DataPremierName)
//      AkkaConfig.DataAnalyserDepartment.PremierPath = dataPremierRef.path.toString
//      val dataPremierActor = context.actorSelection(dataPremierRef.path)
//      val dataResp = dataPremierActor ? CallDataPremier
//      val dataChecksum = this.check.waitAnswer(dataResp, dataPremierRef.path.toString)
//      if (dataChecksum.equals(true)) {
//        log.info("Thank you my man, Data Premier.")
//        dataPremierActor ! MayIUseData
//      }


    case TotalDataNum(content, total) =>
      log.info(s"All data amount is $total")
      this.totalTasksNum = total


    case FinishTask =>
      log.info("")

  }

}
