package sample.persistence

//#persistent-actor-example
import akka.actor._
import akka.persistence._

case class Cmd(data: String)

case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)

  def size: Int = events.length

  override def toString: String = events.reverse.toString
}

class ExampleSubscriber extends Actor {

  override def preStart = context.system.eventStream.subscribe(self, classOf[Evt])

  def receive = {
    case evt: Evt => println(s"${this.self.path} says I have received an amazing event : $evt")
  }
}


class ExamplePersistentActor extends PersistentActor {
  override def persistenceId = "sample-id-1"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents =
    state.size

  val receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
  }

  val receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"${data}-${numEvents}"))(updateState)
      persist(Evt(s"${data}-${numEvents + 1}")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
      }
    case "snap" => saveSnapshot(state)
    case "print" => println(state)
  }

}
//#persistent-actor-example

object PersistentActorExample extends App {

  val system = ActorSystem("example")
  val persistentActor = system.actorOf(Props[ExamplePersistentActor], "persistentActor-4-scala")
  // Subscriber
  val subscriberActor1 = system.actorOf(Props[ExampleSubscriber], "subscriberActor-1-scala")
  // val subscriberActor2 = system.actorOf(Props[ExampleSubscriber], "subscriberActor-2-scala")

  persistentActor ! "print"
  persistentActor ! Cmd("foo")
  persistentActor ! Cmd("baz")
  persistentActor ! Cmd("bar")
  persistentActor ! "snap"
  persistentActor ! Cmd("buzz")
  persistentActor ! "print"

  Thread.sleep(1000)
  system.terminate()
}
