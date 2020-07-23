package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config

object AkkaConfig {

  val SystemName = "CauchySystem"
  val KingName = "King"

  val CrawlerNumber = 2
  val ProducerNumber = 2
  val ConsumerNumber = 2

  object CrawlerDepartment {
    /****
     * Parameters about crawler part.
     */

    val PaladinName = "CrawlerPaladin"
    val SoldierName = "Soldier_"
    val ConsumerGroupID = "crawler"
    val DataSaverName = ""
  }


  object DataAnalyserDepartment {
    /****
     * Parameters about data analyser part.
     */

    val ProducerPaladinName = "KafkaProducerLeader"
    val ProducerSoldierName = "producer_"

    val SearchPaladinName = "KafkaConsumerLeader"
    val SearchSoldierName = "consumer_"

    val ProducerDateSoldierName = "produce_date_soldier"
    val ProducerStockSymbolsSoldierName = "produce_symbol_soldier"

    val SearchDateSoldierName = "consumer_date_soldier"
    val SearchStockSymbolsSoldierName = "consumer_symbol_soldier"
  }

}
