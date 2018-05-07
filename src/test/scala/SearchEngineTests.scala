/**
  * Created by abagla on 5/6/18.
  */

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SearchEngineTests extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val docServer = system.actorOf(Props[DocCrawler], "docServer")
  val reverseIndexServer = system.actorOf(Props[ReverseIndex], "reverseIndex")

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A DocCrawler actor" must {

    "send back inserted doc id" in {
      docServer ! DocCrawler.InsertDoc("1", List("abcd"))
      expectMsg(DocCrawler.IndexInserted("1"))
    }

    "add another doc" in {
      docServer ! DocCrawler.InsertDoc("2", List("hello", "mkdir"))
      expectMsg(DocCrawler.IndexInserted("2"))
    }

    "add one more doc" in {
      docServer ! DocCrawler.InsertDoc("3", List("abcd", "bye"))
      expectMsg(DocCrawler.IndexInserted("3"))
    }

    "add more docs" in {
      docServer ! DocCrawler.InsertDoc("4", List("abcd", "hello", "mkdir"))
      expectMsg(DocCrawler.IndexInserted("4"))
    }

    "overwrite existing doc" in {
      docServer ! DocCrawler.InsertDoc("1", List("abcd", "mkdir"))
      expectMsg(DocCrawler.IndexInserted("1"))
    }

  }

  "Search Query" must {

    "return empty result when no doc is found" in {
      reverseIndexServer !  ReverseIndex.Query(List(""))
      expectMsg(ReverseIndex.QueryResult(Set()))
    }

    "must send back query result" in {
      reverseIndexServer !  ReverseIndex.Query(List("bye"))
      expectMsg(ReverseIndex.QueryResult(Set("3")))
    }

    "must send back query result with multiple doc ids" in {
      reverseIndexServer !  ReverseIndex.Query(List("mkdir"))
      expectMsg(ReverseIndex.QueryResult(Set("1", "2", "4")))
    }

    "must handle conjuction" in {
      // query hello & mkdir
      reverseIndexServer !  ReverseIndex.Query(List("hello", "&", "mkdir"))
      expectMsg(ReverseIndex.QueryResult(Set("2", "4")))
    }

    "must handle disjunction" in {
      // query hello | bye
      reverseIndexServer !  ReverseIndex.Query(List("hello", "|", "bye"))
      expectMsg(ReverseIndex.QueryResult(Set("2", "3", "4")))
    }


    "must combination of conjunction and disjunction" in {
      // query ( hello & mkdir ) | bye
      reverseIndexServer !  ReverseIndex.Query(List("(", "hello", "&", "mkdir", ")", "|", "bye"))
      expectMsg(ReverseIndex.QueryResult(Set("2", "3", "4")))
    }
  }
}
