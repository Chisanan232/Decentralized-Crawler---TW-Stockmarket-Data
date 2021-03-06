package Taiwan_stock_market_crawler_Cauchy.src.main.scala.KafkaMechanism

import java.util
import java.util.Properties

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}


class DataProducerManagement extends KafkaManagement {

  private val props = new Properties()
//  val producer = new KafkaProducer[String, String](this.defineProperties())

  def defineProperties(): Properties = {
    /*
    If developer want to add more configuration or other setting, could overwrite this method.
     */
    this.props.put("bootstrap.servers", "localhost:9092")
    this.props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    this.props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }


  def topicsList(): List[String] = {
    val admin = AdminClient.create(this.defineProperties())
    admin.listTopics().names().get().toArray().map(_.toString).toList
  }


  def topicExists(topic: String): Boolean = {
    val topics = this.topicsList()
    topics.contains(topic)
  }


  private implicit def intToShort(i: Int): Short = i.toShort

  def createOneNewTopic(topicName: String, partitionsNum: Int, replicationsNum: Int): Unit = {
    val admin = AdminClient.create(this.defineProperties())
    val newTopicCondition = new NewTopic(topicName, partitionsNum, replicationsNum)
    admin.createTopics(util.Arrays.asList(newTopicCondition))
  }


  def deleteTopic(topics: String): Unit = {
    val admin = AdminClient.create(this.defineProperties())
    admin.deleteTopics(util.Arrays.asList(topics))
  }


  def topicPartitionsNum(topic: String) (implicit producer: KafkaProducer[String, String]): Int = {
    producer.partitionsFor(topic).size()
  }


  def test(): Unit = {
    val admin = AdminClient.create(this.defineProperties())
  }


  def writeMsg(topic: String, key: String, message: String) (implicit producer: KafkaProducer[String, String]): Unit = {
    val record = new ProducerRecord(topic, key, message)
    producer.send(record)
  }


  //  def writeMsg(topic: String, key: String, message: List[String]) (implicit producer: KafkaProducer[String, String]): Unit = {
  //    message.foreach(msg => {
  //      val record = new ProducerRecord(topic, key, msg)
  //      producer.send(record)
  //    })
  //  }


  def closeSession() (implicit producer: KafkaProducer[String, String]): Unit = {
    producer.close()
  }

}

