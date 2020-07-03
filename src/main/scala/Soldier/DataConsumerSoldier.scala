package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataConsumerManagement

import scala.collection.JavaConverters._

import org.apache.kafka.clients.consumer.KafkaConsumer
import akka.actor.{Actor, ActorLogging, Props}


class DataConsumerSoldier extends Actor with ActorLogging {

  def receive: Receive = {

    case NeedAPIInfo(content, consumerID) =>
      log.info("Roger that!")
      log.info("Start to sniff all data ...")

      // Overwrite method without keyword extend
      // https://stackoverflow.com/questions/11929198/override-a-function-without-extending-the-class
      implicit val groupID = "api-resource"
      val cm = new DataConsumerManagement() {
        override def getMsg(timeoutMins: Int)(implicit consumer: KafkaConsumer[String, String]): Unit =
          while (true) {
            val records = consumer.poll(timeoutMins).asScala
            for (record <- records) {
              log.info(s"$record")
              if ((record.value() != "") || (record.value() != " ")) {
                log.info("Get the API conditions! Send it to Crawler Soldier.")
                val crawlSoldier = context.actorSelection(AkkaConfig.CrawlerDepartment.CrawlSoldierPaths(consumerID))
                crawlSoldier ! TargetAPI("This is API condition.", record.value())
              } else {
                log.info("Got an empty value.")
              }
            }
          }
      }

      implicit val consumerS = new KafkaConsumer[String, String](cm.defineProperties())
      cm.scanOnePartitionMsg(KafkaConfig.APIsTopic, consumerID)
      cm.getMsg(100)


    case CheckingData =>
      log.info("Roger that!")
      log.info("Start to checking data.")

      implicit val groupID = "checking-api-resource"
      val cm = new DataConsumerManagement() {
        override def getMsg(timeoutMins: Int)(implicit consumer: KafkaConsumer[String, String]): Unit =
          while (true) {
            val records = consumer.poll(timeoutMins).asScala
            for (record <- records) {
              println(record)
              if ((record.value() != "") || (record.value() != " ")) {
                log.info("Get the API conditions! Send it to Crawler Soldier.")
                val parentActor = context.actorSelection(context.parent.path)
                parentActor ! CheckingResult("This is check-result.", record.key(), record.value())
              } else {
                log.info("Got an empty value.")
              }
            }
          }
      }

      implicit val consumerS = new KafkaConsumer[String, String](cm.defineProperties())
      cm.scanAllPartitionsMsg(KafkaConfig.DoneDateTimeTopic)
      cm.getMsg(100)

  }

}
