package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data.{APIDate, DataSource}
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataProducerManagement
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.DataConsumerSoldier

import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class DataConsumerPaladin extends Actor with ActorLogging {

  val dataRefreshTimeout = 3
  var dataRefresh: Boolean = false
  var previousDataRefresh: Boolean = false
  var dataRefreshTime: Int = 0
  var previousDataRefreshTime: Int = 0

  val ad = new APIDate
  var allAPIData: Array[String] = ad.targetDateRange().toArray

  val ds = new DataSource
  val stockSymbolList: List[Any] = ds.stockSymbolData()

  var checkMap: Map[String, Array[String]] = Map[String, Array[String]]()
  for (symbol <- stockSymbolList) checkMap += (symbol.toString -> allAPIData)

  private def recordHistory(key: String, date: String): Map[String, Array[String]] = {
    /*
    Update the datetime data after examiner soldiers get the data every time
     */
    val newDateArray = this.checkMap(key).filter(!_.equals(date))
    this.checkMap += (key -> newDateArray)
    this.checkMap
  }

  def receive: Receive = {

    case CallKafkaConsumer =>
      log.info("I Receive task!")
      val consumerLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $consumerLeaderPath"
      sender() ! msg


    case NeedCrawlerCondition =>
      log.info("Checking crawler conditions ...")
      // 1. Check whether Target Topic exist or not. Yes: Start sniff, No: Build new one and sniff
      val pm = new DataProducerManagement
      if (pm.topicExists(KafkaConfig.APIsTopic).equals(false)) {
        // Build new one.
        pm.createOneNewTopic(KafkaConfig.APIsTopic, KafkaConfig.APIsTopicPartitionsNum, KafkaConfig.ReplicationNum)
        // Tell Producer Leader who should generate info and record them to the new topic.
        val producerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath)
        producerPaladin ! GenerateAPI("", context.self.path.toString)
      }

      // Subscribe the topic and sniff for target data.
      // 1. Build up consumer soldiers
      val consumerSoldiers = new Array[ActorRef](AkkaConfig.CrawlerNumber)
      for (consumerID <- 0.until(AkkaConfig.CrawlerNumber)) consumerSoldiers(consumerID) = context.actorOf(Props[DataConsumerSoldier], AkkaConfig.DataAnalyserDepartment.ConsumerSoldierName + s"$consumerID")
      // 2. Start to consumer data of target topic by consumer and send it to crawler solder to crawl data.
      consumerSoldiers.foreach(consumerRef => {
        val consumerPaths = AkkaConfig.DataAnalyserDepartment.ConsumerSoldierPaths
        consumerPaths(consumerRef.path.name.toString.takeRight(1).toInt) = consumerRef.path.toString
        val consumer = context.actorSelection(consumerRef.path)
        consumer ! NeedAPIInfo("", consumerRef.path.name.takeRight(1).toInt)
      })


    case ExamineData =>
      log.info("Start to examine the data which has been exist here and whether they are we wanted or not.")

      // Clear target data
      // 1. Build up consumer soldier to get data we need
      val examineSoldiers = new Array[ActorRef](KafkaConfig.DoneDateTimeTopicPartitionsNum)
      for (examinerID <- 0.until(KafkaConfig.DateTimeTopicPartitionsNum)) examineSoldiers(examinerID) = context.actorOf(Props[DataConsumerSoldier], AkkaConfig.DataAnalyserDepartment.ExamineSoldierName + s"$examinerID")
      examineSoldiers.foreach(examinerRef => {
        // 2. Reduce these data come back to Consumer Paladin and clear that the left API which we doesn't crawl.
        // 3. Generate the left API condition by Producer Paladin
        val examinerPaths = AkkaConfig.DataAnalyserDepartment.ExamineSoldierPaths
        examinerPaths(examinerRef.path.name.toString.takeRight(1).toInt) = examinerRef.path.toString
        val examiner = context.actorSelection(examinerRef.path)
        examiner ! CheckingData
      })

      // After finish checking target data, it start to call crawler soldier to crawl data.
      // Send Notification to Data Premier.
      // Will keep checking the data refresh signal, if it over 30 seconds doesn't change the status, actor will ask
      // soldier start to crawl data.
      val selfActor = context.actorSelection(self.path)
      selfActor ! AlertStart


    case AlertStart =>
      log.info("Enable alert!")
      this.previousDataRefresh = this.dataRefresh
      this.previousDataRefreshTime = this.dataRefreshTime
      // Will wait for several time and send generate and crawl notification to target actor.
      Thread.sleep(1000 * this.dataRefreshTimeout)
      println("********************  ALERT DEBUG ***********************")
      println("[Alert Debug] this actor wait up!")
      println("********************  ALERT DEBUG ***********************")
      if (this.dataRefresh.equals(this.previousDataRefresh) && this.dataRefreshTime.equals(this.previousDataRefreshTime)) {
        println("********************  ALERT DEBUG 2 ***********************")
        val producerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath)
        if (this.dataRefreshTime == 0) {
          println("********************  ALERT DEBUG 3 ***********************")
          producerPaladin ! GenerateAPI("", context.self.path.toString)
        } else {
          println("********************  ALERT DEBUG 4 ***********************")
          producerPaladin ! GenerateLeftAPI("", this.checkMap)
        }
      } else {
        log.info("Data still be refreshed! Will wait for next time to check ...")
      }


    case CheckingResult(content, key, date) =>

      /***
       * How long should I need to use to check?
       * Current Solution: Set a alert to calculate the time, start to crawl if it take so long time that doesn't have any new data.
       */

      log.info("Checking data ...")
      val checkDateResult = this.recordHistory(key, date)
      this.dataRefresh = true
      this.dataRefreshTime += 1

  }

}
