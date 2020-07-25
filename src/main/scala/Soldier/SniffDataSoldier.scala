package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism.DataConsumerManagement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}
import scala.concurrent.duration._

import org.apache.kafka.clients.consumer.KafkaConsumer
import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout


class SniffDataSoldier extends Actor with ActorLogging {

  final val king: String = AkkaConfig.KingName
  final val paladin: String = AkkaConfig.CrawlerDepartment.PaladinName
  final val soldier: String = AkkaConfig.CrawlerDepartment.SoldierName

  override def receive: Receive = {

    case NeedAPIInfo =>
      log.info("\uD83D\uDC4A Roger that!")
      log.info("\uD83D\uDC41 Start to sniff all data ...")

      // Overwrite method without keyword extend
      // https://stackoverflow.com/questions/11929198/override-a-function-without-extending-the-class
      implicit val groupID: String = "api-resource"
      val cm = new DataConsumerManagement() {
        override def getMsg(timeoutMins: Int)(implicit consumer: KafkaConsumer[String, String]): Unit =
          while (true) {
            val records = consumer.poll(timeoutMins).asScala
            for (record <- records) {
              log.info(s"$record")
              if ((record.value() != "") || (record.value() != " ")) {

                log.info("\uD83D\uDCE9 Get the API conditions! Send it to Crawler Soldier.")

                implicit val timeout: Timeout = Timeout(5.seconds)
                context.system.actorSelection(s"user/$king/$paladin/$soldier").resolveOne().onComplete{
                  case Success(crawlerActor) =>
                    log.info("\uD83D\uDCEB AKKA actor exist and send message to it.")
                    crawlerActor ! GotAPI("This is API condition.", record.value())
                  case Failure(exception) =>
                    log.info("\uD83D\uDC94 AKKA actor doesn't exist. Please ensure this error.")
                    throw exception.fillInStackTrace()
                }

              } else {
                log.info("\uD83D\uDC94 Got an empty value.")
              }
            }
          }
      }

      implicit val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](cm.defineProperties())
      cm.subscribeTopic(KafkaConfig.APIsTopic)
      cm.getMsg(100)

  }

}
