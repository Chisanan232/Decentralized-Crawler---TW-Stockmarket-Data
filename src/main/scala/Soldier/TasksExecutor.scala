package Taiwan_stock_market_crawler_Cauchy.src.main.scala.Soldier

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config.AkkaConfig

import scala.sys.process._

import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats


class TasksExecutor {

//  val Path = "src/main/scala/Taiwan_stock_market_crawler_Cauchy/src/main/python"
  val Path: String = if (AkkaConfig.InDocker.equals(true)) {
    "Taiwan_stock_market_crawler_Cauchy/src/main/python"
  } else {
    "src/main/scala/Taiwan_stock_market_crawler_Cauchy/src/main/python"
  }


  private def jsonToMap(jsonData: String): Map[String, Any] = {
    /***
     * Parse Json type value into Map
     * https://stackoverflow.com/questions/29908297/how-can-i-convert-a-json-string-to-a-scala-map
     */

    implicit val formats: DefaultFormats.type = DefaultFormats
    // Method 1
    parse(jsonData).extract[Map[String, Any]]
    // Method 2
//    parse(jsonData).values.asInstanceOf[Map[String, Any]]
  }


  def generateAPIbyPython(date: String, symbol: String): String = {
    val runCmd = s"python3 $Path/multi-lan_stock-crawler_py-ver.py --date $date --listed-company $symbol"
    runCmd.!!
  }


//  def runCode(api: String, date: String, symbol: String): String = {
//    val runningCmd = s"python $Path/multi-lan_stock-crawler_py-ver.py --date $date --listed-company $symbol"
//    println("[INFO] Running Python Code Command Line: \n" + runningCmd)
//    val runningResult = runningCmd.!!
//    println(s"[DEBUG] running command line result: $runningResult")
//    runningResult
//  }


  def runCode(pyCodeParameters: String): (String, String, String) = {
    // 1. Parser Json type value to be a Scala collection "Map"
    val parameters = this.jsonToMap(pyCodeParameters)

    // 2. Get the data which be saved in the collection
    val api = parameters("api").toString
    val apiConditions = parameters("condition")
    val date = apiConditions.asInstanceOf[Map[String, String]]("date")
    val symbol = apiConditions.asInstanceOf[Map[String, String]]("stockNo")

    // 3. Assign the value which we got to the string type value as a command line
    val runningCmd = s"python3 $Path/multi-lan_stock-crawler_py-ver.py --stock-api $api --date $date --listed-company $symbol --sleep enable"
    println("[INFO] Running Python Code Command Line: \n" + runningCmd)
    val runningResult = runningCmd.!!
    println(s"[DEBUG] running command line result: $runningResult")
    (symbol, date, runningResult)
  }

}


//object RunningCrawlerCode extends App {
//
//  val te = new TasksExecutor
//  te.runCode("{\"api\": \"http://www.tse.com.tw/exchangeReport/STOCK_DAY\", \"condition\": {\"response\": \"json\", \"date\": \"20200101\", \"stockNo\": \"0614\"}}")
//
//}
