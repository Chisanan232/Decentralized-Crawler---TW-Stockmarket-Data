package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataProducerManagement
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier.DataProducerSoldier

import org.apache.kafka.clients.producer.KafkaProducer
import akka.actor.{Actor, ActorLogging, ActorRef, Props}


class DataProducerPaladin extends Actor with ActorLogging {

  private val check = new CheckMechanism

  override def receive: Receive = {

    case CallDataProducerPaladin =>
      log.info("I Receive task!")
      val producerLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $producerLeaderPath"
      sender() ! msg


    case GenerateAPI(content, taskNum, stockSymbols, dateTimes) =>
      log.info("Start to generate API conditions data!")
      val stockSymbolProducerRef = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerStockSymbolsSoldierName)
      val stockSymbolProducer = context.actorSelection(stockSymbolProducerRef.path)
      stockSymbolProducer ! ProduceSymbol("Here is the company stock symbol data crawler soldier need.", stockSymbols)

//      val dateProducerRef = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerDateSoldierName)
//      val dateProducer = context.actorSelection(dateProducerRef.path)
//      dateProducer ! ProduceDate("Here is the datetime data crawler soldier need.", allDateList)

      // Calculate total data amount
      val total = stockSymbols.length * dateTimes.length
      sender() ! TotalDataNum("Here is the total amount how many data have we need to crawl.", total)

      println("*************** allDataNum *********************")
      println(taskNum)
//      println("*************** companyNameList *********************")
//      println(companyNameList)
      println("*************** stockSymbolList *********************")
      println(stockSymbols)
      println("*************** allDateList *********************")
      println(dateTimes)

      // Analyse these data and distribute to each producer soldier
      val eachActorTasksNum = taskNum / KafkaConfig.APIsTopicPartitionsNum
//      val companyNameGroupList = companyNameList.grouped(eachActorTasksNum.toInt).toList
      val stockSymbolGroupList = stockSymbols.grouped(eachActorTasksNum.toInt).toList
      //      println("*************** companyNameGroupList *********************")
      //      println(companyNameGroupList)
      println("*************** stockSymbolGroupList *********************")
      println(stockSymbolGroupList)
      val dateTimeGroupList = dateTimes.grouped(eachActorTasksNum.toInt).toList
      println("*************** stockSymbolGroupList *********************")
      println(dateTimeGroupList)

      val producerSoldiers = new Array[ActorRef](KafkaConfig.APIsTopicPartitionsNum)
      for (producerID <- 0.until(KafkaConfig.APIsTopicPartitionsNum)) producerSoldiers(producerID) = context.actorOf(Props[DataProducerSoldier], AkkaConfig.DataAnalyserDepartment.ProducerSoldierName + s"$producerID")
      producerSoldiers.foreach(producerRef => {
        val producer = context.actorSelection(producerRef.path)
        println("[DEBUG] stockSymbolGroupList.toList.apply(producerID): " + stockSymbolGroupList.apply(this.check.getActorIndex(producerRef)))
        producer ! ProduceAPI("This is crawl data task contents.", stockSymbolGroupList.apply(this.check.getActorIndex(producerRef)), dateTimeGroupList.apply(this.check.getActorIndex(producerRef)))
      })


    case FinishAPI(content, stockSymbol, date) =>
      log.info("Good job, crawler soldier!")
      val pm = new DataProducerManagement
      implicit val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](pm.defineProperties())
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
