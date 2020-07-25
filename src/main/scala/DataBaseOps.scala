package Taiwan_stock_market_crawler_Cauchy.src.main.scala

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config.CassandraConfig
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config.DataPart

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


class DataBaseOps {

  // Create Cluster object
  // Should use withoutJMXReporting to avoid error ""
  // https://docs.datastax.com/en/developer/java-driver/3.5/manual/metrics/

  private val cluster = Cluster.builder()
    .withoutJMXReporting()
    .addContactPoint(CassandraConfig.CassandraHost)
    .build()

  def getTables(keyspace: String): List[String] = {
    val tablesInfo = this.cluster.getMetadata().getKeyspace(keyspace).getTables()
    val tables = tablesInfo.toString.split(";,")

    var newTablesInfo = new ListBuffer[String]()
    for((ele, index) <- tables.zipWithIndex) {
      if (index.equals(0)) {newTablesInfo += ele.toString.drop(1)}
      else if (index.equals(tables.length - 1)) {newTablesInfo += ele.toString.filterNot(";]".toSet)}
      else {newTablesInfo += ele}
    }

    newTablesInfo.toList
  }


  def getTablesName(keyspace: String): List[String] = {
    val tablesInfo = this.cluster.getMetadata().getKeyspace(keyspace).getTables()

    val format = Regex.quote(keyspace) + "\\.\\w{1,256} \\(" r
    val tablesName = format.findAllIn(tablesInfo.toString)

    val tablesNameList = new ListBuffer[String]()
    tablesName.foreach(ele => {
       tablesNameList += ele.split("\\.").apply(1).split(" \\(")apply(0)
    })

    tablesNameList.toList
  }


  def createTable(keyspace: String, name: String, part: DataPart): Unit = {
    val session = this.cluster.connect(keyspace)

    /***
     * Table name:
     * company name or stock symbol?
     * Fields:
     * 日期       成交股數          成交金額      開盤價        最高價         最低價         收盤價     漲跌價差    成交筆數
     * date  trade_volume_share   turnover   open_price  highest_price  lowest_price  close_price  change   transaction
     */
    val columns = part.toString match {
      case "StockMarket" =>
        "date varchar , " +
          "trade_volume_share bigint , " +
          "turnover bigint , " +
          "open_price bigint , " +
          "highest_price bigint , " +
          "lowest_price bigint, " +
          "close_price bigint, " +
          "change bigint, " +
          "transaction bigint"
      case _ => println("The parameter 'part' value is incorrect ...")
    }

    val SQLCmd = s"""CREATE TABLE $name ($columns , createdAt int, \"createdAt\" int , PRIMARY KEY (date, createdAt)) ;"""

    session.execute(SQLCmd)
  }


  def deleteTable(keyspace: String, name: String): Unit = {
    val session = this.cluster.connect(keyspace)

    val SQLCmd = s"DROP TABLE $name ;"

    session.execute(SQLCmd)
  }


  def closeSession(): Unit = {
    this.cluster.close()
  }

}
