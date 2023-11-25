package com.example.policemanthiefgame.actors

import NetGraphAlgebraDefs.{Action, NodeObject}
import akka.actor.{Actor, Props}
import com.example.policemanthiefgame.api.{GetState, MovePolice, MoveThief, ResetGame}
import com.example.policemanthiefgame.models.GameState
import com.example.policemanthiefgame.services.GameLogic
import com.example.policemanthiefgame.services.GameLogic.{calculateNextMove, randomNode}
import com.google.common.graph.MutableValueGraph

class GameActor(perturbedGraph: MutableValueGraph[NodeObject, Action], originalGraph: MutableValueGraph[NodeObject, Action]) extends Actor {
  var state: GameState = GameState(None, None) // Game not started

  def receive: Receive = {
    case MoveThief if !state.gameOver =>
      val nextMove = getNextMove(state.thiefPosition, state.policePosition)
      updateGameState(nextMove, isThief = true)
      sender() ! gameStateResponse()

    case MovePolice if !state.gameOver =>
      val nextMove = getNextMove(state.policePosition, state.thiefPosition)
      updateGameState(nextMove, isThief = false)
      sender() ! gameStateResponse()


    case GetState =>
      sender() ! state

    case ResetGame =>
      state = GameState(None, None, result = None, lastPlayer = None)
      sender() ! state

    case _ if state.gameOver =>
      state.lastPlayer match {
        case Some(lastPlayer) =>
          val message = state.result match {
            case Some(msg) if msg.contains("stuck") && lastPlayer == "thief" =>
              println(s"Inside police won")
              "Police won as the opponent got stuck. Game over! Restart the game."
            case Some(msg) if msg.contains("stuck") && lastPlayer == "police" =>
              println(s"Inside thief won")
              "Thief won as the opponent got stuck. Game over! Restart the game."
            case Some(msg) =>
              println(s"Inside some msg")
              println(s"last player : ${lastPlayer}")
              msg // Other game over scenarios
            case None =>
              "Game is over. Please restart to play again."
          }
          sender() ! GameState(None, None, gameOver = true, result = Some(message))

        case None =>
          sender() ! GameState(None, None, gameOver = true, result = Some("Game is over. Please restart to play again."))
      }
    }


  private def getNextMove(currentPosition: Option[NodeObject], opponentPosition: Option[NodeObject]): Option[NodeObject] = {
    currentPosition match {
      case None if opponentPosition.isEmpty =>
        Some(randomNode(perturbedGraph)) // First move of the game
      case None =>
        Some(GameLogic.farthestNodeFrom(opponentPosition.get, perturbedGraph)) // Opponent moved first
      case Some(pos) =>
        calculateNextMove(pos, perturbedGraph, originalGraph) // Subsequent moves
    }
  }
  private def updateGameState(nextMove: Option[NodeObject], isThief: Boolean): Unit = {


    val otherPlayerPosition = if (isThief) state.policePosition else state.thiefPosition

    // Check if the next move results in the thief and police being on the same node
    if (nextMove.exists(nm => otherPlayerPosition.contains(nm))) {
      val positionStr = nextMove.map(_.id.toString).getOrElse("Unknown")
      val otherPositionStr = otherPlayerPosition.map(_.id.toString).getOrElse("Unknown")
      val message = if (isThief) s"Thief caught by police at node $otherPositionStr. Game over!!! Restart to play again."
      else s"Police caught the thief at node $otherPositionStr. Game over!!! Restart to play again."
      state = GameState(None, None, gameOver = true, Some(message))
    }
    // Check if thief lands on a node with valuable data
    else if (isThief && nextMove.exists(_.valuableData)) {
      val thiefPosition = nextMove.map(_.id.toString).getOrElse("Unknown")
      state = GameState(None, None, gameOver = true, Some(s"Congratulations! Thief has found valuable data at node $thiefPosition. Game over. Restart to play again."))
    }
    // Regular game update
    else {
      updatePosition(nextMove, isThief)
    }

    // Update lastPlayer field
    val lastPlayer = if (isThief) "thief" else "police"
    state = state.copy(lastPlayer = Some(lastPlayer))
  }

  private def updatePosition(nextMove: Option[NodeObject], isThief: Boolean): Unit = {
    if (nextMove.isEmpty) {
      val loser = if (isThief) "Thief" else "Police"
      val position = if (isThief) state.thiefPosition else state.policePosition
      val positionStr = position.map(_.id.toString).getOrElse("Unknown")
      val stuckMessage = s"$loser is stuck at node $positionStr and cannot make a move. Game over. Restart the game."
      state = GameState(None, None, gameOver = true, Some(stuckMessage), lastPlayer = Some(loser))
    } else {
      if (isThief) state = state.copy(thiefPosition = nextMove)
      else state = state.copy(policePosition = nextMove)
    }
  }
    private def gameStateResponse(): GameState = {
      if (state.gameOver) {
        // If the game is over, return the current state including the game over message
        state
      } else {
        // If the game is ongoing, return the state with current positions
        GameState(
          thiefPosition = state.thiefPosition,
          policePosition = state.policePosition,
          gameOver = state.gameOver, // Include the gameOver field
          result = state.result, // Include the result field
          lastPlayer = state.lastPlayer // Include the lastPlayer field
        )
      }
    }

  def props(perturbedGraph: MutableValueGraph[NodeObject, Action], originalGraph: MutableValueGraph[NodeObject, Action]): Props =
    Props(new GameActor(perturbedGraph, originalGraph))
}