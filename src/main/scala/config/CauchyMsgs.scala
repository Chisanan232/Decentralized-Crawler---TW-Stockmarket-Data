package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config


trait CauchyMsgs {
  val content: String
}

/***
 * Here messages be classified by sender.
 * @param content: Task content or addition.
 */

// Akka System
case class DataAim(content: String) extends CauchyMsgs  // Receiver: King

// King
case class CallDataProducerPaladin(content: String) extends CauchyMsgs  // Receiver: Producer Paladin
case class CallDataSnifferPaladin(content: String) extends CauchyMsgs  // Receiver: Consumer Paladin
case class CallCrawlerPaladin(content: String) extends CauchyMsgs  // Receiver: Crawler Premier

case class GenerateAPI(content: String, taskNum: Float, stockSymbols: List[Any], dateTimes: List[String]) extends CauchyMsgs  // Receiver: Producer Paladin
case class NeedCrawlerCondition(content: String) extends CauchyMsgs  // Receiver: Consumer Paladin
case class AwaitDataAndCrawl(content: String) extends CauchyMsgs  // Receiver: Crawler Premier


// Crawler Paladin
case class ReadyOnStandBy(content: String) extends CauchyMsgs  // Receiver: Crawler Soldiers


// Data Analyser Premier
case class ExamineData(content: String) extends CauchyMsgs  // Receiver: Consumer Paladin


// Producer Paladin
case class ProduceSymbol(content: String, symbol: List[Any]) extends CauchyMsgs  // Receiver: Producer Soldier
case class TotalDataNum(content: String, total: Int) extends CauchyMsgs  // Receiver: King
case class ProduceAPI(content: String, symbols: List[Any], date: List[Any]) extends CauchyMsgs  // Receiver: Producer Soldier
case class ProduceLeftAPI(content: String, leftAPICondition: Map[String, Array[String]]) extends CauchyMsgs  // Receiver: Producer Soldier


// Consumer Paladin
case class NeedAPIInfo(content: String, consumerID: Int) extends CauchyMsgs  // Receiver: Consumer Soldier (Give info to Crawler Soldier)
case class CheckingData(content: String) extends CauchyMsgs  // Receiver: Consumer Soldier (Check data)
case class AlertStart(content: String) extends CauchyMsgs  // Receiver: Consumer Paladin self.


// Producer Soldier
// No message be send


// Consumer Soldier
case class TargetAPI(content: String, api: String) extends CauchyMsgs  // Receiver: Crawler Soldier
case class CheckingResult(content: String, key: String, date: String) extends CauchyMsgs  // Receiver: Consumer Paladin


// Crawler Soldier
case class ProduceDate(content: String, symbol: String, date: String) extends CauchyMsgs  // Receiver: Producer Soldier


// For Testing or Debug
case class TestMsg(content: String) extends CauchyMsgs  // Receiver:
case class DeBug(content: String) extends CauchyMsgs  // Receiver:


// Here are some message which doesn't be used
case class GotData(content: String) extends CauchyMsgs  // Receiver:
case class FinishTask(content: String) extends CauchyMsgs  // Receiver:

case class GetTarget(content: String) extends CauchyMsgs  // Receiver:

case class FinishAPI(content: String, stockSymbol: String, date: String) extends CauchyMsgs  // Receiver:

case class APIDone(content: String) extends CauchyMsgs  // Receiver:
case class APIDataReady(content: String) extends CauchyMsgs  // Receiver:
