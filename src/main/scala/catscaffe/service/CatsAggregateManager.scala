package catscaffe.service

import java.net.URLEncoder

import akka.actor.Props
import catscaffe.domain.AggregateRoot.{GetState, Remove}
import catscaffe.domain.CatAggregate
import catscaffe.domain.CatAggregate.{Eat, AddCat, Play}
import catscaffe.service.CatsAggregateManager._

/**
 * Created by aflorea on 28.09.2016.
 */
object CatsAggregateManager {

  import AggregateManager._

  /** These are a special breed of surrogates DTOs */
  case class AddCatToHerd(catname: String, breed_id: Int) extends Command

  case class FeedCat(id: String, quantityOfFood: Double) extends Command

  case class PlayWithCat(id: String, minutes: Int) extends Command

  case class GetCat(id: String) extends Command

  case class AdoptCat(id: String) extends Command

  /** */
  def props: Props = Props(new CatsAggregateManager)

}


class CatsAggregateManager extends AggregateManager {

  def processCommand = {
    case AddCatToHerd(catname, breed_id) =>
      val id = URLEncoder.encode(catname, "UTF-8")
      processAggregateCommand(id, AddCat(catname, breed_id))

    case FeedCat(id, quantityOfFood) =>
      processAggregateCommand(id, Eat(quantityOfFood))

    case PlayWithCat(id, minutes) =>
      processAggregateCommand(id, Play(minutes))

    case GetCat(id) =>
      processAggregateCommand(id, GetState)

    case AdoptCat(id) =>
      processAggregateCommand(id, Remove)
  }

  override def aggregateProps(id: String) = CatAggregate.props(id)
}
