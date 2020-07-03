package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config

object AkkaConfig {

  val SystemName = "CauchySystem"
  val KingName = "King"
  var KingPath = ""

  val CrawlerNumber = 2
  val ProducerNumber = 2
  val ConsumerNumber = 2

  object CrawlerDepartment {
    /****
     * Parameters about crawler part.
     */

    val CrawlPremierName = "CrawlerPremier"
    var PremierPath = ""
    val CrawlerPaladinName = "CrawlerPaladin"
    var CrawlerPaladinPath = ""
    val CrawlSoldierName = "Soldier_"
    var CrawlSoldierPaths = new Array[String](CrawlerNumber)
    val ConsumerGroupID = "crawler"
    val DataSaverName = ""
    val DataSaverPaths = new Array[String](CrawlerNumber)
  }


  object DataAnalyserDepartment {
    /****
     * Parameters about data analyser part.
     */

    val DataPremierName = "DataPremier"
    var PremierPath = ""

    val ProducerPaladinName = "KafkaProducerLeader"
    var ProducerPaladinPath = ""
    val ProducerSoldierName = "producer_"
    var ProducerSoldierPaths = new Array[String](KafkaConfig.APIsTopicPartitionsNum)

    val ConsumerPaladinName = "KafkaConsumerLeader"
    var ConsumerPaladinPath = ""
    val ConsumerSoldierName = "consumer_"
    var ConsumerSoldierPaths = new Array[String](CrawlerNumber)

    val ProducerDateSoldierName = "produce_date_soldier"
    var ProducerDateSoldierPath = ""
    val ProducerStockSymbolsSoldierName = "produce_symbol_soldier"
    var ProducerStockSymbolsSoldierPath = ""

    val ConsumerDateSoldierName = "consumer_date_soldier"
    var ConsumerDateSoldierPath = ""
    val ConsumerStockSymbolsSoldierName = "consumer_symbol_soldier"
    var ConsumerStockSymbolsSoldierPath = ""

    val ExamineSoldierName = "examiner_"  // This is consumer team
    var ExamineSoldierPaths = new Array[String](KafkaConfig.DateTimeTopicPartitionsNum)
  }

}
