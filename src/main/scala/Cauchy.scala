package Taiwan_stock_market_crawler_Cauchy.src.main.scala

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.King.CauchyKing

import akka.actor.{ActorSystem, Props}


object Cauchy {

  def main(args: Array[String]): Unit = {
    val UsingDockerEnv = args(0)
    UsingDockerEnv.toString match {
      case "enable" => AkkaConfig.InDocker = true
      case "disable" => AkkaConfig.InDocker = false
      case _ => AkkaConfig.InDocker = false
    }
    println(s"[INFO] The environment variable 'UseDockerEnv' value is $UsingDockerEnv")

      val system = ActorSystem(AkkaConfig.SystemName)
      val KingActor = system.actorOf(Props[CauchyKing], AkkaConfig.KingName)
      KingActor ! DataAim
  }

}
