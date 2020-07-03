package Taiwan_stock_market_crawler_Cauchy.src.main.scala

import Taiwan_stock_market_crawler_Cauchy.src.main.scala.config._
import Taiwan_stock_market_crawler_Cauchy.src.main.scala.King.CauchyKing

import akka.actor.{ActorSystem, Props}


object Cauchy extends App {

  val system = ActorSystem(AkkaConfig.SystemName)
  val KingActor = system.actorOf(Props[CauchyKing], AkkaConfig.KingName)
  KingActor ! DataAim

}
