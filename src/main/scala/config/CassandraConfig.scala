package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config

object CassandraConfig {

  val CassandraHost = "127.0.0.1"
  val CassandraPort = "9042"
  val CassandraMaster = "local[*]"
  val CassandraAppName = "Stock_Market_Cassandra"

  val Keyspace = "stock-market"

}
