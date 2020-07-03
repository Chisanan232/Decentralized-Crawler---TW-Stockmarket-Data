package Taiwan_stock_market_crawler_Cauchy.src.main.scala

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.util.matching.Regex

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


  def actorPathExists(actorName: String): Boolean = {
    import scala.util.control.Breaks.{breakable, break}

    println("Start to wait for the target path exist ...")
    // Here you need to know the target path exist or not.
    breakable(
      while (true) {

        // Make sure which path you should target by actor name.
        val path = actorName match {
          // King
          case AkkaConfig.KingName => AkkaConfig.KingPath
          // Crawler group
          case AkkaConfig.CrawlerDepartment.CrawlPremierName => AkkaConfig.CrawlerDepartment.PremierPath
          case AkkaConfig.CrawlerDepartment.CrawlerPaladinName => AkkaConfig.CrawlerDepartment.CrawlerPaladinPath
          case AkkaConfig.CrawlerDepartment.CrawlSoldierName => AkkaConfig.CrawlerDepartment.CrawlSoldierPaths
          case AkkaConfig.CrawlerDepartment.DataSaverName => AkkaConfig.CrawlerDepartment.DataSaverPaths
          // Data analyser group
          case AkkaConfig.DataAnalyserDepartment.DataPremierName => AkkaConfig.DataAnalyserDepartment.PremierPath
          case AkkaConfig.DataAnalyserDepartment.ProducerPaladinName => AkkaConfig.DataAnalyserDepartment.ProducerPaladinPath
          case AkkaConfig.DataAnalyserDepartment.ProducerSoldierName => AkkaConfig.DataAnalyserDepartment.ProducerSoldierPaths
          case AkkaConfig.DataAnalyserDepartment.ConsumerPaladinName => AkkaConfig.DataAnalyserDepartment.ConsumerPaladinPath
          case AkkaConfig.DataAnalyserDepartment.ConsumerSoldierName => AkkaConfig.DataAnalyserDepartment.ConsumerSoldierPaths
          case AkkaConfig.DataAnalyserDepartment.ExamineSoldierName => AkkaConfig.DataAnalyserDepartment.ExamineSoldierPaths
        }

        println("=====================================")
        println(s"Target path is $path")
        println("=====================================")

        // Determine whether it's a array or not
        // https://stackoverflow.com/questions/37824267/scala-test-whether-any-variable-can-be-iterated-over
        path match {
          case path: Array[_] =>
            // This is array type value.
            if (path.isEmpty) {
              println("Will sleep for wait for it ...")
              // Wait for it
              Thread.sleep(500)
            } else {
              println("Got the target path !")
              break()
            }
          case _ =>
            // This is string type value.
            if (path.equals("") || path.equals(" ")) {
              println("Will sleep for wait for it ...")
              // Wait for it
              Thread.sleep(500)
            } else {
              println("Got the target path !")
              break()
            }
        }

      }
    )
    true
  }

}
