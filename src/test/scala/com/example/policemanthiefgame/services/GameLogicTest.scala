import NetGraphAlgebraDefs.{Action, NodeObject}
import com.example.policemanthiefgame.services.GameLogic
import com.google.common.graph.{MutableValueGraph, ValueGraphBuilder}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.JavaConverters._


class GameLogicTest extends AnyFlatSpec with Matchers {

  private def createSampleGraph(): MutableValueGraph[NodeObject, Action] = {
    val graph = ValueGraphBuilder.directed().build[NodeObject, Action]()

    // Adding nodes similar to your mock data
    val node1 = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true)
    val node2 = NodeObject(2, 1, 5, 1, 12, 0, 5, 15, 0.27581271574709965, false)
    val node3 = NodeObject(3, 6, 4, 1, 17, 4, 5, 13, 0.8877319178342418, true)
    val node4 = NodeObject(4, 0, 7, 1, 96, 3, 4, 6, 0.10908621261855633, false)
    // ... add other nodes as needed

    List(node1, node2, node3, node4).foreach(graph.addNode)

    // Define some actions to simulate edges between nodes
    val action1 = Action(15, node1, node2, 51, 43, Some(35), 0.24903749272559117)
    val action2 = Action(18, node2, node3, 13, 0, Some(24), 0.5603602596600947)
    val action3 = Action(20, node3, node4, 21, 5, Some(30), 0.7891011121314151)
    val action4 = Action(25, node4, node1, 31, 6, Some(40), 0.4567891234567891)
    // ... add more actions to create a network of nodes

    // Adding edges between nodes
    graph.putEdgeValue(node1, node2, action1)
    graph.putEdgeValue(node2, node3, action2)
    graph.putEdgeValue(node3, node4, action3)
    graph.putEdgeValue(node4, node1, action4)
    // ... add more edges as necessary

    graph
  }

  private def createSamplePerturbedGraph(): MutableValueGraph[NodeObject, Action] = {
    val graph = ValueGraphBuilder.directed().build[NodeObject, Action]()

    // Adding nodes similar to your original graph, with some differences
    val node1 = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, false)
    val node2 = NodeObject(2, 1, 5, 1, 12, 0, 5, 15, 0.27581271574709965, false)
    val node5 = NodeObject(5, 4, 3, 2, 20, 2, 3, 12, 0.6543210987654321, true) // Different node from the original graph
    val node6 = NodeObject(6, 2, 8, 1, 10, 1, 7, 10, 0.9876543210987654, true) // Another different node

    List(node1, node2, node5, node6).foreach(graph.addNode)

    // Define some actions to simulate edges between nodes, with some differences
    val action1 = Action(15, node1, node2, 51, 43, Some(35), 0.24903749272559117)
    val action5 = Action(22, node2, node5, 20, 1, Some(25), 0.5432109876543210) // Different action from original graph
    val action6 = Action(30, node5, node6, 25, 8, Some(45), 0.8765432109876543) // Another different action

    // Adding edges between nodes
    graph.putEdgeValue(node1, node2, action1)
    graph.putEdgeValue(node2, node5, action5)
    graph.putEdgeValue(node5, node6, action6)

    graph
  }


  "randomNode" should "return a node from the graph" in {
    val graph = createSampleGraph()
    val node = GameLogic.randomNode(graph)
    graph.nodes().contains(node) shouldBe true
  }

  "farthestNodeFrom" should "return a node far from the start node" in {
    val graph = createSampleGraph()

    val startNode = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true)
    val distantNode = GameLogic.farthestNodeFrom(startNode, graph)

    // Assertion 1: Distant node is not the same as start node
    assert(distantNode != startNode, "The distant node should not be the same as the start node.")

    // Assertion 2: Distant node is not a direct successor of start node
    assert(!graph.successors(startNode).contains(distantNode), "The distant node should not be a direct successor of the start node.")
  }


  "calculateConfidenceScore" should "return a score based on node similarity" in {
    // Create two NodeObject instances
    val node1 = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true)
    val node2 = NodeObject(1, 3, 16, 1, 72, 3, 4, 2, 0.3742537211515221, true) // Identical to node1

    // Calculate the confidence score between node1 and node2
    val score = GameLogic.calculateConfidenceScore(node1, node2)

    // Assertions
    score should be >= 0.0
    score should be <= 1.0

    // Since node1 and node2 are identical, the score should be 1.0
    score shouldBe 1.0
  }

  "calculateNextMove" should "return the best next move based on confidence scores" in {
    // Assuming createSampleGraph is a method that creates a sample graph
    val originalGraph = createSampleGraph()
    val perturbedGraph = createSamplePerturbedGraph() // Create a perturbed graph for testing

    // Select a current node that exists in both graphs
    val currentNodeId = originalGraph.nodes().asScala.headOption.map(_.id).getOrElse(-1)
    val currentNode = perturbedGraph.nodes().asScala.find(_.id == currentNodeId).getOrElse {
      fail(s"Current node with ID $currentNodeId not found in the perturbed graph.")
    }

    val nextNode = GameLogic.calculateNextMove(currentNode, perturbedGraph, originalGraph)

    // Assertions
    nextNode shouldBe defined // Check that a next node is returned
    nextNode.get should not be currentNode // The next node should be different from the current node
  }

}
