package catscaffe.views

import akka.actor.ActorLogging
import akka.persistence.{PersistentView, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

/**
  * Created by acflorea on 28/09/2016.
  */
class CatsView extends PersistentView with ActorLogging {
  private var numReplicated = 0

  override def persistenceId: String = "Fely"

  override def viewId = "fely's-view"

  def receive = {
    case SnapshotOffer(metadata, snapshot: Int) =>
      numReplicated = snapshot
      log.debug(s"view received snapshot offer ${snapshot} (metadata = ${metadata})")
    case payload if isPersistent =>
      numReplicated += 1
      log.debug(s"view replayed event ${payload} (num replicated = ${numReplicated})")
    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"view saved snapshot (metadata = ${metadata})")
    case SaveSnapshotFailure(metadata, reason) =>
      log.debug(s"view snapshot failure (metadata = ${metadata}), caused by ${reason}")
    case payload =>
      log.debug(s"view received other message ${payload}")
  }

}
