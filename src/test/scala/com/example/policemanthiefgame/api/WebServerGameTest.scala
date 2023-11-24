import NetGraphAlgebraDefs.NodeObject
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import com.example.policemanthiefgame.api.{GetState, MovePolice, MoveThief, ResetGame, WebServerGame}
import com.example.policemanthiefgame.models.GameState
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec

class WebServerGameTest extends AnyWordSpec with Matchers with ScalatestRouteTest {

  // Mock the ActorSystem and TestProbe for mocking the GameActor
  override def createActorSystem(): ActorSystem = ActorSystem("WebServerGameTest")
  implicit val timeout: Timeout = Timeout(5.seconds)
  val gameActorProbe = TestProbe()

  "WebServerGame" should {
    "respond to move thief path" in {
      // Mock the GameActor response
      gameActorProbe.setAutoPilot((sender, msg) => msg match {
        case MoveThief =>
          // Example GameState with a realistic NodeObject
          val exampleNode = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true)
          sender ! GameState(Some(exampleNode), None, false, None, Some("thief"))
          TestActor.KeepRunning
        case _ => TestActor.NoAutoPilot
      })

      // Define the route using a mocked GameActor
      val mockedRoute = WebServerGame.route(gameActorProbe.ref)

      // Perform the test
      Get("/move/thief") ~> mockedRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] should include("GameState") // Check for a valid response
      }
    }
    "respond to move police path" in {
      // Mock the GameActor response for the MovePolice message
      gameActorProbe.setAutoPilot((sender, msg) => msg match {
        case MovePolice =>
          val exampleNode = NodeObject(2, 1, 5, 1, 12, 0, 5, 15, 0.27581271574709965, false)
          sender ! GameState(None, Some(exampleNode), false, None, Some("police"))
          TestActor.KeepRunning
        case _ => TestActor.NoAutoPilot
      })

      val mockedRoute = WebServerGame.route(gameActorProbe.ref)

      Get("/move/police") ~> mockedRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] should include("GameState")
      }
    }

    "respond to get state path" in {
      // Mock the GameActor response for the GetState message
      gameActorProbe.setAutoPilot((sender, msg) => msg match {
        case GetState =>
          val exampleThiefNode = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true)
          val examplePoliceNode = NodeObject(2, 1, 5, 1, 12, 0, 5, 15, 0.27581271574709965, false)
          sender ! GameState(Some(exampleThiefNode), Some(examplePoliceNode), false, None, Some("thief"))
          TestActor.KeepRunning
        case _ => TestActor.NoAutoPilot
      })

      val mockedRoute = WebServerGame.route(gameActorProbe.ref)

      Get("/state") ~> mockedRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] should include("GameState")
      }
    }

    "respond to reset game path" in {
      // Mock the GameActor response for the ResetGame message
      gameActorProbe.setAutoPilot((sender, msg) => msg match {
        case ResetGame =>
          sender ! GameState(None, None, gameOver = false)
          TestActor.KeepRunning
        case _ => TestActor.NoAutoPilot
      })

      val mockedRoute = WebServerGame.route(gameActorProbe.ref)

      Get("/reset") ~> mockedRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] should include("Game reset. Ready to start a new game.")
      }
    }
  }
}
