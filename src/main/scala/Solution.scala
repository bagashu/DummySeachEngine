/**
  * Created by abagla on 5/5/18.
  */

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn._


object Solution extends App {

  val system = ActorSystem("searchSystem")
  implicit val timeout = Timeout(30.seconds)
  val docServer = system.actorOf(Props[DocCrawler], "docServer")
  val reverseIndexServer = system.actorOf(Props[ReverseIndex], "reverseIndex")

  while (!system.isTerminated) {
    readLine()
      .split(" ").toList match {
      case "index" :: x :: tail  if x.forall(_.isDigit) && tail.forall(_.forall(_.isLetterOrDigit)) =>
        // potential timeout failure when system in high load. Switch to tell pattern.
        ask(docServer, DocCrawler.InsertDoc(x, tail))
          .map {
            case DocCrawler.IndexInserted(id) => println(s"index ok $id")
          }

      case "index" :: x :: _  if !x.forall(_.isDigit) =>
        println("index error doc id is not an integer")

      case "index" :: _ :: tail  if !tail.forall(_.forall(_.isLetterOrDigit)) =>
        println("index error token contains a non-alphanumeric character")

      case "query" :: tail =>
        ask(reverseIndexServer, ReverseIndex.Query(tail))
          .map {
            case ReverseIndex.QueryResult(result) => println(s"query results ${result.mkString(" ")}")
          }.recover {
          case e: Exception => println(s"query error ${e.getMessage}")
        }
      case "quit" :: Nil | "exit" :: Nil => system.terminate

      case "" :: Nil =>

      case _ => println("Invalid Input")
    }
  }
}
