package Taiwan_stock_market_crawler_Cauchy.src.main.scala

import scala.concurrent.{Await, Awaitable}
import scala.util.matching.Regex

import akka.actor.ActorRef
import akka.util.Timeout


class CheckMechanism {

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


  def getActorIndex(actorRef: ActorRef): Int = {
    val indexFormatter = "[0-9]{1,7}".r
    indexFormatter.findAllIn(actorRef.path.name.toString).toList.last.toInt
  }

}
