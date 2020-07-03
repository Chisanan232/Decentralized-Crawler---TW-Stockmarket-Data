package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.consumer.KafkaConsumer


trait KafkaManagement {

  def defineProperties(): Properties

}
