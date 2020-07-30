package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config.DataPart

trait CauchyMsgs {
  val content: String
}

/***
 * Here messages be classified by sender.
 * @param content: Task content or addition.
 */

// Akka System
final case class DataAim(content: String) extends CauchyMsgs  // Receiver: King

// King
final case class CallDataProducerPaladin(content: String) extends CauchyMsgs  // Receiver: Producer Paladin
final case class CallDataSnifferPaladin(content: String) extends CauchyMsgs  // Receiver: Consumer Paladin
final case class CallCrawlerPaladin(content: String) extends CauchyMsgs  // Receiver: Crawler Premier
final case class CallDataSaverPaladin(content: String) extends CauchyMsgs  // Receiver: Data Saver Paladin

final case class GenerateAPI(content: String, taskNum: BigInt, stockSymbols: List[Any], dateTimes: List[String]) extends CauchyMsgs  // Receiver: Producer Paladin
final case class NeedCrawlerCondition(content: String) extends CauchyMsgs  // Receiver: Consumer Paladin
final case class AwaitDataAndCrawl(content: String, allTaskNum: BigInt) extends CauchyMsgs  // Receiver: Crawler Premier


// Crawler Paladin
final case class ReadyOnStandBy(content: String) extends CauchyMsgs  // Receiver: Crawler Soldiers
final case class TargetAPI(content: String, api: String) extends CauchyMsgs  // Receiver: Crawler Soldier
final case class ClearData(content: String) extends CauchyMsgs


// Producer Data Paladin
final case class ProduceSymbol(content: String, symbol: List[Any]) extends CauchyMsgs  // Receiver: Producer Soldier
final case class TotalDataNum(content: String, total: Int) extends CauchyMsgs  // Receiver: King
final case class ProduceAPI(content: String, symbols: List[Any], date: List[Any]) extends CauchyMsgs  // Receiver: Producer Soldier


// Sniff Data Paladin
final case class NeedAPIInfo(content: String) extends CauchyMsgs  // Receiver: Consumer Soldier (Give info to Crawler Soldier)


// Data Saver Paladin
final case class SaveFinish(content: String) extends CauchyMsgs


// Producer Soldier
// No message be send


// Sniff Data Soldier
final case class GotAPI(content: String, api: String) extends CauchyMsgs  // Receiver: Crawler Soldier


// Crawler Soldier
final case class SaveData(content: String, dataType: DataPart, data: Any) extends CauchyMsgs  // Receiver: Cassandra Soldier (Give info to Crawler Soldier)
final case class FinishCurrentJob(content: String, actor: String) extends CauchyMsgs  // Receiver: Crawler Paladin



// For Testing or Debug
final case class TestMsg(content: String) extends CauchyMsgs  // Receiver:
final case class DeBug(content: String) extends CauchyMsgs  // Receiver:


// Here are some message which doesn't be used
final case class GotData(content: String) extends CauchyMsgs  // Receiver:
final case class FinishTask(content: String) extends CauchyMsgs  // Receiver:

final case class GetTarget(content: String) extends CauchyMsgs  // Receiver:

final case class FinishAPI(content: String, stockSymbol: String, date: String) extends CauchyMsgs  // Receiver:

final case class APIDone(content: String) extends CauchyMsgs  // Receiver:
final case class APIDataReady(content: String) extends CauchyMsgs  // Receiver:
