package catscaffe.domain

import akka.actor._
import akka.persistence._
import catscaffe.common.Acknowledge

object AggregateRoot {

  trait State

  case object Uninitialized extends State

  case object Removed extends State

  trait Event

  trait Command

  case object Remove extends Command

  case object GetState extends Command

  /**
   * We don't want the aggregate to be killed if it hasn't fully restored yet,
   * thus we need some non AutoReceivedMessage that can be handled by akka persistence.
   */
  case object KillAggregate extends Command

  /**
   * Specifies how many events should be processed before new snapshot is taken.
   */
  val eventsPerSnapshot = 3
}

/**
 * Base class for other aggregates.
 * It includes such functionality as:
 * - snapshot management
 * - publishing applied events to Event Bus
 * - handling processor recovery.
 *
 */
trait AggregateRoot extends PersistentActor with ActorLogging {

  import AggregateRoot._

  override def persistenceId: String

  protected var state: State = Uninitialized

  private var eventsSinceLastSnapshot = 0

  /**
   * Updates internal processor state according to event that is to be applied.
   *
   * @param evt Event to apply
   */
  def updateState(evt: Event): Unit

  /**
   * This method should be used as a callback handler for persist() method.
   *
   * @param evt Event that has been persisted
   */
  protected def afterEventPersisted(evt: Event): Unit = {
    log.debug(s"$persistenceId :: {} event received", evt)
    updateAndRespond(evt)
    publish(evt)
    eventsSinceLastSnapshot += 1
    if (eventsSinceLastSnapshot >= eventsPerSnapshot) {
      log.debug(s"$persistenceId :: {} events reached, saving snapshot $state", eventsPerSnapshot)
      saveSnapshot(state)
      eventsSinceLastSnapshot = 0
    }
  }

  private def updateAndRespond(evt: Event): Unit = {
    updateState(evt)
    respond()
  }

  protected def respond(): Unit = {
    sender() ! state
    context.parent ! Acknowledge(persistenceId)
  }

  private def publish(event: Event) =
    context.system.eventStream.publish(event)

  override val receiveRecover: Receive = {
    case evt: Event =>
      log.debug(s"$persistenceId :: recover {}", evt)
      eventsSinceLastSnapshot += 1
      updateState(evt)
    case SnapshotOffer(metadata, state: State) =>
      restoreFromSnapshot(metadata, state)
      log.debug(s"recovering aggregate from snapshot $state")
  }

  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: State)

}