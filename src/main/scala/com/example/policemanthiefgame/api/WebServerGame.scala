package com.example.policemanthiefgame.api

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.example.policemanthiefgame.actors.GameActor
import com.example.policemanthiefgame.client.AutomatedGameClient
import com.example.policemanthiefgame.models.GameState
import com.example.policemanthiefgame.utils.GraphLoader
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

// Messages
case object MoveThief
case object MovePolice
case object GetState
case object ResetGame

object WebServerGame {
  private val logger = LoggerFactory.getLogger(getClass)

  def route(gameActor: ActorRef)(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Route = {
    concat(
      path("move" / "thief") {
        get {
          val move: Future[String] = (gameActor ? MoveThief).mapTo[GameState].map(gameStateResponse)
          complete(move)
        }
      },
      path("move" / "police") {
        get {
          val move: Future[String] = (gameActor ? MovePolice).mapTo[GameState].map(gameStateResponse)
          complete(move)
        }
      },
      path("state") {
        get {
          val state: Future[String] = (gameActor ? GetState).mapTo[GameState].map(gameStateResponse)
          complete(state)
        }
      },
      path("reset") {
        get {
          val reset: Future[String] = (gameActor ? ResetGame).mapTo[GameState].map(_ => "Game reset. Ready to start a new game.")
          complete(reset)
        }
      },
      path("automated-play") {
        get {
          Future {
            AutomatedGameClient.playGame()
          }(ExecutionContext.global) // Use a suitable execution context
          complete("Automated play started.")
        }
      }
    )
  }

  def gameStateResponse(state: GameState): String = {
    state.result.getOrElse(
      s"GameState(Thief Position: ${state.thiefPosition.map(_.id).getOrElse("Unknown")}, Police Position: ${state.policePosition.map(_.id).getOrElse("Unknown")})"
    )
  }

  def start(arg1: String, arg2: String, arg3: String): Unit = {
    logger.info("Starting WebServer Game")

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val executionContext = system.dispatcher

    // Load the graph
    val originalGraph = GraphLoader.loadGraph(arg1)
    val perturbedGraph = GraphLoader.loadGraph(arg2)

    // Instantiate the game actor
    val gameActor = system.actorOf(Props(new GameActor(perturbedGraph, originalGraph)), "gameActor")

    // Start the server with the route
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route(gameActor))

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def main(args: Array[String]): Unit = {
    start(args(0), args(1), args(2))
  }
}
