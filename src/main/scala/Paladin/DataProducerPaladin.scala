package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data.{APIDate, DataSource}
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataProducerManagement
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.DataProducerSoldier

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class DataProducerPaladin extends Actor with ActorLogging {

  def receive: Receive = {

    case CallKafkaProducer =>
      log.info("I Receive task!")
      val producerLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $producerLeaderPath"
      sender() ! msg


    case GenerateAPI(content, sourceActor) =>
      log.info("There nothing in topic, start to generate api conditions data!")
      // Read json type data to get all stock symbol of company which in Taiwan
      println("+++++++++++++++++++++++++ Spark beginning point +++++++++++++++++++++")
      val ds = new DataSource
      println("+++++++++++++++++++++++++ Spark end point +++++++++++++++++++++")
      println("+++++++++++++++++++ Start to get data by Spark ! +++++++++++++++++++")
      val allDataNum = ds.dataNumber()
      val companyNameList = ds.companyData()
      val stockSymbolList = ds.stockSymbolData()

      val stockSymbolProducerRef = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerStockSymbolsSoldierName)
      AkkaConfig.DataAnalyserDepartment.ProducerStockSymbolsSoldierPath = stockSymbolProducerRef.path.toString
      val stockSymbolProducer = context.actorSelection(stockSymbolProducerRef.path)
      stockSymbolProducer ! ProduceSymbol("Here is the company stock symbol data crawler soldier need.", stockSymbolList)

      // Generate datetime data.
      val ad = new APIDate
      val allDateList = ad.targetDateRange()

      val dateProducerRef = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerDateSoldierName)
      AkkaConfig.DataAnalyserDepartment.ProducerDateSoldierPath = dateProducerRef.path.toString
      val dateProducer = context.actorSelection(dateProducerRef.path)
//      dateProducer ! ProduceDate("Here is the datetime data crawler soldier need.", allDateList)

      // Calculate total data amount
      val total = stockSymbolList.length * allDateList.length
      sender() ! TotalDataNum("Here is the total amount how many data have we need to crawl.", total)

      println("*************** allDataNum *********************")
      println(allDataNum)
      println("*************** companyNameList *********************")
      println(companyNameList)
      println("*************** stockSymbolList *********************")
      println(stockSymbolList)
      println("*************** allDateList *********************")
      println(allDateList)
      println(s"Source actor path is $sourceActor")

      ds.close_spark()

      // Analyse these data and distribute to each producer soldier
      val eachActorTasksNum = allDataNum / KafkaConfig.APIsTopicPartitionsNum
      val companyNameGroupList = companyNameList.grouped(eachActorTasksNum.toInt).toList
      val stockSymbolGroupList = stockSymbolList.grouped(eachActorTasksNum.toInt).toList
      println("*************** companyNameGroupList *********************")
      println(companyNameGroupList)
      println("*************** stockSymbolGroupList *********************")
      println(stockSymbolGroupList)

      val producerSoldiers = new Array[ActorRef](KafkaConfig.APIsTopicPartitionsNum)
      for (producerID <- 0.until(KafkaConfig.APIsTopicPartitionsNum)) producerSoldiers(producerID) = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerSoldierName + s"$producerID")
      producerSoldiers.foreach(producerRef => {
        val producerPaths = AkkaConfig.DataAnalyserDepartment.ProducerSoldierPaths
        val producerID = producerRef.path.name.toString.takeRight(1).toInt
        producerPaths(producerID) = producerRef.path.toString
        val producer = context.actorSelection(producerRef.path)
        println(s"[DEBUG] producerID: $producerID")
        println("[DEBUG] stockSymbolGroupList.toList.apply(producerID): " + stockSymbolGroupList.apply(producerID))
        producer ! ProduceAPI("This is crawl data task contents.", stockSymbolGroupList.apply(producerID), allDateList)
      })


    case GenerateLeftAPI(content, leftAPICondition) =>
      log.info("")

      val allDataNum = leftAPICondition.keys.toList.length
      val leftAPISymbolsGroup = leftAPICondition.keys.toList.grouped(allDataNum / KafkaConfig.APIsTopicPartitionsNum).toList

      val producerSoldiers = new Array[ActorRef](KafkaConfig.APIsTopicPartitionsNum)
      for (producerID <- 0.until(KafkaConfig.APIsTopicPartitionsNum)) producerSoldiers(producerID) = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerSoldierName + s"$producerID")
      producerSoldiers.foreach(producerRef => {
        val producerPaths = AkkaConfig.DataAnalyserDepartment.ProducerSoldierPaths
        val producerID = producerRef.path.name.toString.takeRight(1).toInt
        producerPaths(producerID) = producerRef.path.toString
        val producer = context.actorSelection(producerRef.path)
        val targetLeftAPICondition = leftAPICondition.filterKeys(leftAPISymbolsGroup.contains(_).equals(true))
        producer ! ProduceLeftAPI("This is crawl data task contents.", targetLeftAPICondition)
      })


    case FinishAPI(content, stockSymbol, date) =>
      log.info("Good job, crawler soldier!")
      val pm = new DataProducerManagement
      implicit val producer = new KafkaProducer[String, String](pm.defineProperties())
      if (pm.topicExists(KafkaConfig.DoneDateTimeTopic).equals(false)) {
        // Build new one and record data.
        pm.createOneNewTopic(KafkaConfig.DoneDateTimeTopic, KafkaConfig.DoneDateTimeTopicPartitionsNum, KafkaConfig.ReplicationNum)
        pm.writeMsg(KafkaConfig.DoneDateTimeTopic, stockSymbol, date)
      } else {
        // Record data
        pm.writeMsg(KafkaConfig.DoneDateTimeTopic, stockSymbol, date)
      }

  }

}
