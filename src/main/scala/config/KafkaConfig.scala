package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config


object KafkaConfig {

  // Kafka broken node
//  val BrokenNode = "localhost:9092"
  val BrokenNode = "172.20.10.3:9092"
  val ReplicationNum = 1

  // Kafka topic list
  val RunningLogging = "crawler-running-log"
  val RunningLoggingPartitionsNum = 2

  //  val CompanyStockSymbolTopic = "company-stock-symbol"
  val CompanyStockSymbolTopicPartitionsNum = 2

  //  val DateTimeTopic = "data-datetime"
  val DateTimeTopicPartitionsNum = 2

  //  val DoneDateTimeTopic = "done-data-datetime"
  val DoneDateTimeTopicPartitionsNum = 2

  //  val APIsTopic = "target-crawl-APIs"
  val APIsTopicPartitionsNum = 2

  //  val DataTopic = "stock-data"
  val DataTopicPartitionsNum = 2

  val CompanyStockSymbolTopic = "test-company-stock-symbol"
  val DateTopic = "test-data-datetime"
  val DoneDateTimeTopic = "test-done-data-datetime"
  val APIsTopic = "test-target-crawl-APIs"
  val DataTopic = "test-stock-data"

}
