package catscaffe.test

import akka.actor.{ActorSystem, Props}
import catscaffe.domain.CatAggregate.Play
import catscaffe.service.CatsAggregateManager
import catscaffe.service.CatsAggregateManager.{AddCatToHerd, FeedCat, PlayWithCat}

/**
 * Created by aflorea on 28.09.2016.
 */
object CatsHerding extends App {

  val system = ActorSystem("catcaffe")
  val herdManager = system.actorOf(Props[CatsAggregateManager], "cats-herd-manager")

  // Subscriber
  // val subscriberActor1 = system.actorOf(Props[ExampleSubscriber], "subscriberActor-1-scala")
  // val subscriberActor2 = system.actorOf(Props[ExampleSubscriber], "subscriberActor-2-scala")

  herdManager ! AddCatToHerd("Fely", 1)
  herdManager ! AddCatToHerd("Bri", 1)
  herdManager ! AddCatToHerd("Barek", 1)

  herdManager ! PlayWithCat("Fely", 10)
  herdManager ! PlayWithCat("Fely", 5)
  herdManager ! PlayWithCat("Fely", 5)
  herdManager ! PlayWithCat("Fely", 5)

  herdManager ! FeedCat("Fely", 10)
  herdManager ! FeedCat("Fely", 5)
  herdManager ! FeedCat("Fely", 5)


  Thread.sleep(1000)
  system.terminate()

}
