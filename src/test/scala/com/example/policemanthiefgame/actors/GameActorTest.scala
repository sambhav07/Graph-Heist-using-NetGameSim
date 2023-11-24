import NetGraphAlgebraDefs.{Action, NodeObject}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.example.policemanthiefgame.actors.GameActor
import com.example.policemanthiefgame.api.{GetState, MovePolice, MoveThief, ResetGame}
import com.example.policemanthiefgame.models.GameState
import com.google.common.graph.{MutableValueGraph, ValueGraphBuilder}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike


class GameActorTest extends TestKit(ActorSystem("GameActorTest")) with ImplicitSender
  with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  // Create a small set of NodeObjects

  val mockOriginalGraph: MutableValueGraph[NodeObject, Action] = {
    val graph = ValueGraphBuilder.directed().build[NodeObject, Action]()

    // Define some NodeObjects (mock data)
    val node1 = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true)
    val node2 = NodeObject(2, 1, 5, 1, 12, 0, 5, 15, 0.27581271574709965, false)
    val node3 = NodeObject(3, 6, 4, 1, 17, 4, 5, 13, 0.8877319178342418, true)

    // Add nodes to the graph
    List(node1, node2, node3).foreach(graph.addNode)

    // Define some Actions (mock edges)
    val action1 = Action(15, node1, node2, 51, 43, Some(35), 0.24903749272559117)
    val action2 = Action(18, node2, node3, 13, 0, Some(24), 0.5603602596600947)

    // Add edges to the graph
    graph.putEdgeValue(node1, node2, action1)
    graph.putEdgeValue(node2, node3, action2)

    graph
  }

  val mockPerturbedGraph: MutableValueGraph[NodeObject, Action] = {
    val graph = ValueGraphBuilder.directed().build[NodeObject, Action]()

    // Define some NodeObjects (mock data)
    val node1 = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, false)
    val node2 = NodeObject(2, 1, 5, 1, 12, 0, 5, 15, 0.27581271574709965, false)
    val node4 = NodeObject(4, 0, 7, 1, 96, 3, 4, 6, 0.10908621261855633, false) // Different node from original graph

    // Add nodes to the graph
    List(node1, node2, node4).foreach(graph.addNode)

    // Define some Actions (mock edges)
    val action1 = Action(15, node1, node2, 51, 43, Some(35), 0.24903749272559117)
    val action3 = Action(12, node2, node4, 0, 24, None, 0.2752278156565915) // Different action from original graph

    // Add edges to the graph
    graph.putEdgeValue(node1, node2, action1)
    graph.putEdgeValue(node2, node4, action3)

    graph
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A GameActor" must {
    "reset the game state when receiving a ResetGame message" in {
      val gameActor = system.actorOf(Props(new GameActor(mockPerturbedGraph, mockOriginalGraph)))
      gameActor ! ResetGame
      expectMsg(GameState(None, None, gameOver = false))
    }
  }
  "A GameActor" must {
    "update thief position when receiving a MoveThief message" in {
      val gameActor = system.actorOf(Props(new GameActor(mockPerturbedGraph, mockOriginalGraph)))

      // Check initial state
      gameActor ! GetState
      expectMsgPF() {
        case GameState(None, None, false, None, None) => // Initial state is as expected
        case _ =>
          fail("Initial state of the game is not as expected")
      }

      // Send MoveThief message
      gameActor ! MoveThief
      val receivedState = expectMsgType[GameState]

      // Check if the last player to move is the thief
      assert(receivedState.lastPlayer.contains("thief"), "Last player should be 'thief'")
    }
  }
  "A GameActor" must {
    "update police position when receiving a MovePolice message" in {
      val gameActor = system.actorOf(Props(new GameActor(mockPerturbedGraph, mockOriginalGraph)))

      // Send the MovePolice message to the actor
      gameActor ! MovePolice

      // Expect a GameState message in response
      val receivedState = expectMsgType[GameState]

      // Check if the game is still ongoing
      assert(!receivedState.gameOver, "Game should not be over")

      // Check if the last player to move is the police
      assert(receivedState.lastPlayer.contains("police"), "Last player should be 'police'")

      // Check that the police's position is updated (not None)
      assert(receivedState.policePosition.isDefined, "Police's position was not updated")
    }
  }

  "A GameActor" must {
    "return the correct game state when receiving a GetState message" in {
      val gameActor = system.actorOf(Props(new GameActor(mockPerturbedGraph, mockOriginalGraph)))
      gameActor ! GetState
      expectMsgType[GameState] // Validate the received state is as expected
    }
  }

  "A GameActor" must {
    "end the game if the thief or police get stuck" in {
      val gameActor = system.actorOf(Props(new GameActor(mockPerturbedGraph, mockOriginalGraph)))

      // Reset the game to start from a known state
      gameActor ! ResetGame
      expectMsg(GameState(None, None, gameOver = false))

      var gameOver = false

      // Loop to make moves until game over or a certain number of moves have been made
      for (_ <- 1 to 100) { // Example limit to 100 moves to prevent infinite loop
        if (!gameOver) {
          gameActor ! MoveThief // or MovePolice
          val state = expectMsgType[GameState]
          if (state.gameOver) {
            // Check the reason for game over, e.g., someone got stuck
            assert(state.result.exists(_.contains("stuck")), "Game should end because someone got stuck")
            gameOver = true
          }
        }
      }
      // Assert that the game did end
      assert(gameOver, "Game did not end as expected")
    }
  }


}
