import ReverseIndex.StoreReverseIndex
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.Timeout

import scala.collection.mutable.{Map => myMutableMap}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


trait ReverseIndexService {
  self: Actor =>

  implicit val timeout = Timeout(30.seconds)
  def reverseIndexService: Future[ActorRef] =
    context.system.actorSelection("user/" + "reverseIndex")
      .resolveOne().mapTo[ActorRef]
}

/**
  * Created by abagla on 5/5/18.
  */
object DocCrawler{
  sealed trait Indexing
  case class InsertDoc(id: String, tokens: List[String]) extends Indexing
  case class IndexInserted(id: String) extends Indexing
}


class DocCrawler extends Actor
  with ReverseIndexService
  with ActorLogging {

  import DocCrawler._
  val reverseIndexActor = reverseIndexService

  val docIdMap: myMutableMap[String, List[String]] = myMutableMap.empty

  override def receive: Receive = {
    case InsertDoc(id, tokens) =>
      docIdMap(id) = tokens
      reverseIndexActor.map{_ ! StoreReverseIndex(tokens, id)}
      sender() ! IndexInserted(id)
  }
}
