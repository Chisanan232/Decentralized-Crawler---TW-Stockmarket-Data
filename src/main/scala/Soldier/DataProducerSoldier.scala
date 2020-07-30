package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataProducerManagement

import org.apache.kafka.clients.producer.KafkaProducer
import akka.actor.{Actor, Props, ActorLogging}


class DataProducerSoldier extends Actor with ActorLogging {

  def receive: Receive = {

    case ProduceSymbol(content, symbol) =>
      log.info("")
      val pm = new DataProducerManagement
      implicit val producer = new KafkaProducer[String, String](pm.defineProperties())
      if (pm.topicExists(KafkaConfig.CompanyStockSymbolTopic).equals(false)) {
        pm.createOneNewTopic(KafkaConfig.CompanyStockSymbolTopic, KafkaConfig.CompanyStockSymbolTopicPartitionsNum, KafkaConfig.ReplicationNum)
      }
      symbol.foreach(s => {
        pm.writeMsg(KafkaConfig.CompanyStockSymbolTopic, s.toString, s.toString)
      })
      pm.closeSession()


    /** Write message about API pre-data info to Kafka **/
    case ProduceAPI(content, symbols, date) =>
      log.info("Receive task! Start to generate API info!")
      log.info("=+=+=+=+=+=+=+= This is all data this actor need to handle =+=+=+=+=+=+=+=")
      log.info(s"$symbols")
      log.info(s"$date")
      log.info("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=")
      // 1. Call Python method to generate API conditions info which I need
      // 2. Save these info into topic of Kafka server
      val pm = new DataProducerManagement
      implicit val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](pm.defineProperties())
      val te = new TasksExecutor
      symbols.foreach(symbol => {
        date.foreach(d => {
          val APIInfo = te.generateAPIbyPython(d.toString, symbol.toString)
          pm.writeMsg(KafkaConfig.APIsTopic, symbol.toString, APIInfo)
//          log.info(APIInfo)
        })
      })
      pm.closeSession()

  }

}
