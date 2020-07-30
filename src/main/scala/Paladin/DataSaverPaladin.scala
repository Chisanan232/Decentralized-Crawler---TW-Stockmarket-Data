package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Paladin

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config.{DataPart, ListedCompanyInfo, StockMarket}
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data.DataSource
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.DataBaseOps

import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats

import akka.actor.{Actor, ActorLogging}
import org.apache.spark.{SparkConf, SparkContext}
import com.datastax.spark.connector._
import com.datastax.spark.connector.ColumnSelector


class DataSaverPaladin extends Actor with ActorLogging {

  // Save data to database Cassandra has 2 methods
  // Method 1
  private val conf = new SparkConf(true)
    .set("spark.cassandra.connection.host", CassandraConfig.CassandraHost)
    .set("spark.cassandra.connection.port", CassandraConfig.CassandraPort)
    .setMaster(CassandraConfig.CassandraMaster)
    .setAppName(CassandraConfig.CassandraAppName)
  private val sc = new SparkContext(conf)

  private def parseData(data: String): Map[String, Any] = {
    implicit val dataFormatter: DefaultFormats.type = DefaultFormats
    parse(data).extract[Map[String, Any]]
  }


  implicit def IntToBigInt(data: (Int, Int, Int, Int, Int, Int, Int, Int, Int)): (BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt) =
    (BigInt(data._1), BigInt(data._2), BigInt(data._3), BigInt(data._4), BigInt(data._5), BigInt(data._6), BigInt(data._7), BigInt(data._8), BigInt(data._9))

  def writeOneData(keyspace: String, dataType: DataPart, table: String, data: Any): Unit = {
    val dbColumns = dataType.toString match {
      case "ListedCompanyInfo" =>
        SomeColumns("stock_symbol", "company", "ISIN", "listed_data", "listed_type", "industry_type", "CFICode")
      case "StockMarket" =>
        SomeColumns("date", "trade_volume_share", "turnover", "open_price", "highest_price", "lowest_price", "close_price", "change", "transaction")
      case _ => println("The data value is invalid for this database schema, please check it.")
    }

    val scDataframe = this.sc.parallelize(Seq(data))
    scDataframe.saveToCassandra(keyspace, table, dbColumns.asInstanceOf[ColumnSelector])
  }


  def writeMultiData(keyspace: String, dataType: DataPart, table: String, data: List[Any]): Unit = {
    /***
     * How to do some operators with Seq type data
     * https://alvinalexander.com/scala/seq-class-methods-examples-syntax/
     */
    dataType.toString match {
      case "ListedCompanyInfo" =>
        var s: Seq[(String, String, String, String, String, String, String)] = Nil
        data.foreach(d => {
          s = s :+ d.asInstanceOf[(String, String, String, String, String, String, String)]
          // Colon site is the original Seq type data and plus site is new data. From Scala official documentation, there is
          // another operator '+:'
          //      s = d +: s
        })
        val scDataframe = this.sc.parallelize(s)
        scDataframe.saveToCassandra(keyspace, table, SomeColumns("stock_symbol", "company", "ISIN", "listed_data", "listed_type", "industry_type", "CFICode"))

      case "StockMarket" =>
        var s: Seq[(BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt)] = Nil
        data.foreach(d => {
          s = s :+ d.asInstanceOf[(BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt)]
        })
        val scDataframe = this.sc.parallelize(s)
        scDataframe.saveToCassandra(keyspace, table, SomeColumns("date", "trade_volume_share", "turnover", "open_price", "highest_price", "lowest_price", "close_price", "change", "transaction"))

      case _ =>
        println("The data value is invalid for this database schema, please check it.")
    }
  }


  // Method 2
  private val ds = new DataSource
  private val dbo = new DataBaseOps

  private def saveData(keyspace: String, table: String, part: DataPart, data: String): Unit = {
    // Method 2 to save data  to database Cassandra

    if (this.dbo.getTablesName(keyspace).contains(table).equals(false)) this.dbo.createTable(keyspace, table, part)
    this.ds.saveDataToCassandra(keyspace, table, this.ds.convertJsonToDF(data))
  }

  override def receive: Receive = {

    /** Initial CrawlerPaladin AKKA actor **/
    case CallDataSaverPaladin =>
      log.info("I Receive task!")
      val dataSaverLeaderPath = context.self.path
      val msg = s"I'm ready! I'm $dataSaverLeaderPath"
      sender() ! msg


    case SaveData(content, dataType, data) =>
      log.info("Receive data.")
      log.info(s"The data type is $dataType")

      AkkaConfig.CrawlSaverPattern.toString match {
        case "CSVFile" =>
          log.info("Will write data to file as CSV type file.")
          // Not finish
        case "DataBase" =>
          log.info("Will write data to database -- Cassandra.")
          this.writeOneData(CassandraConfig.Keyspace, dataType, CassandraConfig.tableStockData, data)
          sender() ! SaveFinish
        case _ =>
          log.info("The saver type is incorrect. Please check it.")
      }
      log.info("✨ Save data successfully.")

  }

}
