package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Data

import org.apache.spark.{SparkContext, sql}
import org.apache.spark.sql.SparkSession


/***
 * Original data operators
 */
class DataSource {

  val DataFilePath = "src/main/scala/Taiwan_stock_market_crawler_Cauchy/src/main/resources/all_listed_company.json"

  val spark: SparkSession = SparkSession.builder()
    .appName("Taiwan Stock Market decentralized crawler")
    .master("local[*]")
    .getOrCreate()

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


  def closeSpark(): Unit = {
    /*
    https://stackoverflow.com/questions/50504677/java-lang-interruptedexception-when-creating-sparksession-in-scala
     */
    this.spark.sparkContext.stop()
    this.spark.close()
  }

}


//object DataProcessTest extends App {
//
//  val dst = new DataSource
//  val eachActorTasksNum = dst.dataNumber() / KafkaConfig.APIsTopicPartitionsNum
//  println(dst.stockSymbolData().grouped(eachActorTasksNum.toInt))
//  println("**********************************")
//  println(dst.stockSymbolData().grouped(eachActorTasksNum.toInt).foreach(println))
//  println("**********************************")
//  println(dst.stockSymbolData().grouped(eachActorTasksNum.toInt).toList)
//  dst.close_spark()
//
//}
