package catscaffe.test

import akka.actor.{ActorSystem, Props}
import catscaffe.service.CatsAggregateManager
import catscaffe.service.CatsAggregateManager.{AddCatToHerd, FeedCat, GetCat, PlayWithCat}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by aflorea on 28.09.2016.
 */
object CatsHerding extends App {

  implicit val timeout = Timeout(5 seconds) // needed for `?` below

  val system = ActorSystem("catcaffe")
  val herdManager = system.actorOf(Props[CatsAggregateManager], "cats-herd-manager")

  // Subscriber
  // val subscriberActor1 = system.actorOf(Props[ExampleSubscriber], "subscriberActor-1-scala")
  // val subscriberActor2 = system.actorOf(Props[ExampleSubscriber], "subscriberActor-2-scala")

//  herdManager ? AddCatToHerd("Fely", 1) map println
//  herdManager ? AddCatToHerd("Bri", 1) map println
//  herdManager ? AddCatToHerd("Barek", 1) map println

//  herdManager ? PlayWithCat("Fely", 10) map println
  herdManager ? PlayWithCat("Fely", 5) map println
//  herdManager ? PlayWithCat("Bri", 5) map println
//  herdManager ? PlayWithCat("Fely", 5) map println

//  herdManager ? FeedCat("Fely", 10) map println
//  herdManager ? FeedCat("Fely", 5) map println
//  herdManager ? FeedCat("Barek", 5) map println
//  herdManager ? FeedCat("Bri", 5) map println

  Thread.sleep(1000)
  system.terminate()

}
