package catscaffe.domain

import akka.actor.Props
import akka.persistence.SnapshotMetadata
import catscaffe.domain.AggregateRoot._
import catscaffe.domain.CatAggregate._
import catscaffe.domain.CatAggregate.HungerLevel.HungerLevel

/**
 * An Cat aggregate
 */
object CatAggregate {

  import AggregateRoot._

  /**
   * Possible Hunger levels
   */
  object HungerLevel extends Enumeration {

    type HungerLevel = Value
    val feedMeNow = Value(4)
    val darnHungry = Value(3)
    val hungry = Value(2)
    val ish = Value(1)
    val meh = Value(0)
  }

  /** Aggregate specific state values */
  case class Cat(id: String,
                 catname: String = "",
                 breed_id: Int,
                 hungerLevel: HungerLevel
                ) extends State

  /** Aggregate specific commands */
  case class AddCat(catname: String, breed_id: Int) extends Command

  case class Eat(quantityOfFood: Double) extends Command

  case class Play(minutes: Int) extends Command

  /** Aggregate specific events */
  case class CatAdded(catname: String, breed_id: Int, hungerLevel: HungerLevel) extends Event

  case class CatFed(quantityOfFood: Double) extends Event

  case class CatPlayed(minutes: Int) extends Event

  /** There is no delete ... the cat got adopted by a nice family */
  case object CatAdopted extends Event

  /** Configurable object used to create an Actor */
  def props(id: String): Props = Props(new CatAggregate(id))

}


class CatAggregate(id: String) extends AggregateRoot {

  override def persistenceId = id

  override def updateState(evt: AggregateRoot.Event): Unit = evt match {
    case CatAdded(catname, breed_id, hungerLevel) =>
      context.become(created)
      state = Cat(id, catname, breed_id, hungerLevel)

    case CatFed(quantityOfFood) =>
      state match {
        case c: Cat =>
          val newState = c.copy(hungerLevel = feed(c.hungerLevel, quantityOfFood))
          state = newState
        case _ => // no cat to feed
      }

    case CatPlayed(minutes) =>
      state match {
        case c: Cat =>
          val newState = c.copy(hungerLevel = play(c.hungerLevel, minutes))
          state = newState
        case _ => // no cat to feed
      }

    case CatAdopted =>
      context.become(removed)
      state = Removed
  }

  /** Valid commands in an uninitialized state (before creation)
   * - Initialize
   * - GetState
   * - KillAggregate
   * */
  val initial: Receive = {
    case AddCat(catname, breed_id) =>
      // All new cats are hungry
      persist(CatAdded(catname, breed_id, HungerLevel.hungry))(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  /** Valid commands in a created state (after creation)
   * - FeedCat
   * - PlayWithCat
   * - Remove
   * - GetState
   * - KilAggregate
   * */
  val created: Receive = {
    case Eat(quantityOfFood) =>
      persist(CatFed(quantityOfFood))(afterEventPersisted)
    case Play(minutes) =>
      persist(CatPlayed(minutes))(afterEventPersisted)
    case Remove =>
      persist(CatAdopted)(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  /** Valid events in a removed state (after adoption)
   * - GetState
   * - KilAggregate
   * */
  val removed: Receive = {
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  val receiveCommand: Receive = initial

  override def restoreFromSnapshot(metadata: SnapshotMetadata, state: State) = {
    this.state = state
    state match {
      case Uninitialized => context become initial
      case Removed => context become removed
      case _: Cat => context become created
    }
  }

  /**
   * Adjust hunger level depending on the received food
   *
   * @param level
   * @param foodReceived
   * @return
   */
  def feed(level: HungerLevel, foodReceived: Double): HungerLevel = {
    foodReceived match {
      case q if q > 10 =>
        level match {
          case HungerLevel.feedMeNow => HungerLevel.ish
          case HungerLevel.darnHungry => HungerLevel.meh
          // You'll have a very fat cat! stop!
          case HungerLevel.hungry => HungerLevel.meh
          case HungerLevel.ish => HungerLevel.meh
          case HungerLevel.meh => HungerLevel.meh
        }
      case q if q > 5 =>
        level match {
          case HungerLevel.feedMeNow => HungerLevel.hungry
          case HungerLevel.darnHungry => HungerLevel.ish
          case HungerLevel.hungry => HungerLevel.meh
          // You'll have a very fat cat! stop!
          case HungerLevel.ish => HungerLevel.meh
          case HungerLevel.meh => HungerLevel.meh
        }
      case q if q > 0 =>
        level match {
          case HungerLevel.feedMeNow => HungerLevel.feedMeNow
          case HungerLevel.darnHungry => HungerLevel.hungry
          case HungerLevel.hungry => HungerLevel.ish
          case HungerLevel.ish => HungerLevel.meh
          // You'll have a very fat cat! stop!
          case HungerLevel.meh => HungerLevel.meh
        }
      case q => // wtf!!! the cat is not both hungry and furious
        HungerLevel.feedMeNow
    }
  }


  /**
   * Adjust hunger level depending on the time spent playing
   *
   * @param level
   * @param minutes
   * @return
   */
  def play(level: HungerLevel, minutes: Int): HungerLevel = {
    minutes match {
      case m if m > 30 =>
        level match {
          // what are you trying to do, kill the poor hungry cat ?
          case HungerLevel.feedMeNow => HungerLevel.feedMeNow
          case HungerLevel.darnHungry => HungerLevel.feedMeNow
          case HungerLevel.hungry => HungerLevel.feedMeNow
          // It's ok now
          case HungerLevel.ish => HungerLevel.feedMeNow
          case HungerLevel.meh => HungerLevel.darnHungry
        }
      case m if m > 15 =>
        level match {
          // what are you trying to do, kill the poor hungry cat ?
          case HungerLevel.feedMeNow => HungerLevel.feedMeNow
          case HungerLevel.darnHungry => HungerLevel.feedMeNow
          // It's ok now
          case HungerLevel.hungry => HungerLevel.feedMeNow
          case HungerLevel.ish => HungerLevel.darnHungry
          case HungerLevel.meh => HungerLevel.hungry
        }
      case m if m > 0 =>
        level match {
          // what are you trying to do, kill the poor hungry cat ?
          case HungerLevel.feedMeNow => HungerLevel.feedMeNow
          // It's ok now
          case HungerLevel.darnHungry => HungerLevel.feedMeNow
          case HungerLevel.hungry => HungerLevel.darnHungry
          case HungerLevel.ish => HungerLevel.hungry
          case HungerLevel.meh => HungerLevel.ish
        }
    }
  }


}
