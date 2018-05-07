import akka.actor.{Actor, ActorLogging}

import scala.collection.mutable.{Map => myMutableMap}


object ReverseIndex{
  sealed trait Indexing
  case class StoreReverseIndex(tokens: List[String], id: String) extends Indexing
  case class Query(query: List[String]) extends Indexing
  case class QueryResult(query: Set[String]) extends Indexing

}

class ReverseIndex extends Actor with ActorLogging {

  import ReverseIndex._

  //reverse index of token with doc id and its count(rank)
  val reverseIndexMap: myMutableMap[String, myMutableMap[String, Int]] = myMutableMap.empty

  def receive: Receive = {
    case StoreReverseIndex(tokens: List[String], id: String) =>
      tokens.foreach {
        token => reverseIndexMap.update(token,
          reverseIndexMap
            .get(token)
            .fold(myMutableMap(id -> 1)) {y=>
              y.updated(id,y.get(id).fold(1){z => z + 1})})}

    case Query(xs) =>
      // Add query validation checks...
      val queryResult = ParseQuery(xs).parse.eval(reverseIndexMap)
      sender() ! QueryResult(queryResult)
  }
}
