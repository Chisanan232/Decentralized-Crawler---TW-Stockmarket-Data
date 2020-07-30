package Taiwan_stock_market_crawler_Cauchy.src.main.scala

import scala.concurrent.{Await, Awaitable}
import scala.util.matching.Regex

import akka.actor.ActorRef
import akka.util.Timeout


class CheckMechanism {

  /** Handle wait process for actor about whether actor alive or not **/
  def waitAnswer(Response: Awaitable[Any], actorPath: String)(implicit timeout: Timeout): Boolean = {
    val Result = Await.result(Response, timeout.duration)
    if (Result != None) {
      val crawlerFormat = Regex.quote(actorPath).r
      val checksum = crawlerFormat.findFirstIn(Result.toString)
      if (checksum.isDefined) {
        true
      } else {
        false
      }
    } else {
      false
    }
  }


  /** Get the index from AKKA Actor name **/
  def getActorIndex(actorRef: ActorRef): Int = {
    val indexFormatter = "[0-9]{1,7}".r
    indexFormatter.findAllIn(actorRef.path.name.toString).toList.last.toInt
  }

}
