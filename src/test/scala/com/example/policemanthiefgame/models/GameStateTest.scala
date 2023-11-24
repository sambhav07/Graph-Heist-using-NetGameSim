import NetGraphAlgebraDefs.NodeObject
import com.example.policemanthiefgame.models.GameState
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameStateTest extends AnyFlatSpec with Matchers {

  "GameState" should "correctly represent an initial game state" in {
    val initialState = GameState(None, None, gameOver = false)

    initialState.thiefPosition shouldBe None
    initialState.policePosition shouldBe None
    initialState.gameOver shouldBe false
    initialState.result shouldBe None
    initialState.lastPlayer shouldBe None
  }

  it should "correctly represent a game state with thief and police positions" in {
    val thiefNode = NodeObject(1, 2, 3, 4, 5, 6, 7, 8, 0.5, true)
    val policeNode = NodeObject(2, 3, 4, 5, 6, 7, 8, 9, 0.6, false)
    val gameState = GameState(Some(thiefNode), Some(policeNode), gameOver = false)

    gameState.thiefPosition shouldBe Some(thiefNode)
    gameState.policePosition shouldBe Some(policeNode)
    gameState.gameOver shouldBe false
  }

  it should "correctly represent a game over state with a result" in {
    val resultMessage = "Thief won"
    val gameOverState = GameState(None, None, gameOver = true, result = Some(resultMessage))

    gameOverState.gameOver shouldBe true
    gameOverState.result shouldBe Some(resultMessage)
  }

  it should "track the last player who made a move" in {
    val lastPlayer = "thief"
    val gameState = GameState(None, None, gameOver = false, lastPlayer = Some(lastPlayer))

    gameState.lastPlayer shouldBe Some(lastPlayer)
  }
}
