package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Premier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.CheckMechanism
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin.{DataProducerPaladin, DataConsumerPaladin}


import scala.concurrent.duration._

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout
import akka.pattern.ask


class DataPremier extends Actor with ActorLogging {

  val check = new CheckMechanism

  def receive: Receive = {

    case CallDataPremier =>
      val dataPremierPath = context.self.path
      val msg = s"I'm ready! I'm $dataPremierPath"
      log.info(msg)
      sender() ! msg

      // Call Producer and Consumer
      implicit val timeout = Timeout(10.seconds)

      val producerPaladinRef = context.actorOf(Props[DataProducerPaladin], AkkaConfig.DataAnalyserDepartment.ProducerPaladinName)
      AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath = producerPaladinRef.path.toString
      val producerPaladin = context.actorSelection(producerPaladinRef.path)
      val producerResp = producerPaladin ? CallKafkaProducer
      val producerChecksum = this.check.waitAnswer(producerResp, producerPaladinRef.path.toString)
      if (producerChecksum.equals(true)) {
        log.info("Thank you my man, Data Producer Leader.")
      }

      val consumerPaladinRef = context.actorOf(Props[DataConsumerPaladin], AkkaConfig.DataAnalyserDepartment.ConsumerPaladinName)
      AkkaConfig.DataAnalyserDepartment.ConsumerPaladinPath = consumerPaladinRef.path.toString
      val consumerPaladin = context.actorSelection(consumerPaladinRef.path)
      val consumerResp = consumerPaladin ? CallKafkaConsumer
      val consumerChecksum = this.check.waitAnswer(consumerResp, consumerPaladinRef.path.toString)
      if (consumerChecksum.equals(true)) {
        log.info("Thank you my man, Data Consumer Leader.")
      }


    case MayIUseData =>
      log.info("Start to examine data condition you need ...")
      if (check.actorPathExists(AkkaConfig.DataAnalyserDepartment.ConsumerPaladinName).equals(true)) {
        val consumerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ConsumerPaladinPath)
        consumerPaladin ! ExamineData
      } else {
        // Raise an error about cannot find the target path
      }

    //      val producerPaladin = context.actorSelection(AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath)
//      producerPaladin ! GenerateAPI("", context.self.path.toString)

  }

}
