package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data

import java.nio.file.{Files, Paths}

import org.apache.spark.{SparkContext, sql}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._


class FileOpts {

  val DataSaverPath = "src/main/scala/Cafe_GoogleMap_Crawler/crawl-data/"

  def ensureDirPathExist(dirPath: String): Unit = {
    val dir = Paths.get(this.DataSaverPath + dirPath)
    if (! Files.exists(dir)) Files.createDirectory(dir)
  }

}


/***
 * Original data operators
 */
class DataSource {

  val DataFilePath = "src/main/scala/Taiwan_stock_market_crawler_Cauchy/src/main/resources/all_listed_company.json"

  val spark: SparkSession = SparkSession.builder()
    .appName("Taiwan Stock Market decentralized crawler")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  private val fileIO = new FileOpts

  private def readData(): sql.DataFrame = {
//    spark.read.option("multiline", "true").json(this.DataFilePath)
//    this.spark.read.json(spark.sparkContext.wholeTextFiles(this.DataFilePath).values)
    this.spark.read.json(this.DataFilePath)
  }


  def dataNumber(): Float = {
    val data = this.readData()
    data.count()
  }


  def stockSymbolData(): List[Any] = {
    val data = this.readData()
    data.select("stock_symbol").rdd.map(_.toSeq.toList).collect().flatten.toList
  }


  def companyData(): List[Any] = {
    val data = this.readData()
    data.select("company").rdd.map(_.toSeq.toList).collect().flatten.toList
  }


  def convertJsonToDF(data: String): DataFrame = {

    // How to convert Json type data to SQL DataFrame via Spark
    // https://stackoverflow.com/questions/38271611/how-to-convert-json-string-to-dataframe-on-spark
    // http://spark.apache.org/docs/2.2.0/sql-programming-guide.html#json-datasets

    Seq(data).toDF()
  }


  def saveDataToJsonFile(table: String, index: Int, data: DataFrame): Unit = {
    // 1. Check whether the target directory path exist or not
    val tableDir = this.fileIO.DataSaverPath + table
    this.fileIO.ensureDirPathExist(tableDir)
    val jsonFile = tableDir + s"/index_$index.json"
    Files.createFile(Paths.get(jsonFile))

    // 2. Save data into file in target directory
    // https://sparkbyexamples.com/spark/spark-read-and-write-json-file/
    data.write.json(jsonFile)
  }


  def saveDataToCassandra(keyspace: String, table: String, data: DataFrame): Unit = {

    // Save data to database Cassandra methods
    // https://stackoverflow.com/questions/41248269/inserting-data-into-cassandra-table-using-spark-dataframe

    data.write.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspace, "table" -> table)).save()
  }


  def closeSpark(): Unit = {
    /*
    https://stackoverflow.com/questions/50504677/java-lang-interruptedexception-when-creating-sparksession-in-scala
     */
    this.spark.sparkContext.stop()
    this.spark.close()
  }

}
