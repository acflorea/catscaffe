package catscaffe.listeners

import akka.actor.{Actor, ActorLogging}
import catscaffe.domain.AggregateRoot.Event

/**
  * Created by acflorea on 28/09/2016.
  */
class CatListener extends Actor with ActorLogging{
  override def preStart = context.system.eventStream.subscribe(self, classOf[Event])

  def receive = {
    case evt: Event => log.debug(s"${this.self.path} says I have received an amazing event : $evt")
  }
}


