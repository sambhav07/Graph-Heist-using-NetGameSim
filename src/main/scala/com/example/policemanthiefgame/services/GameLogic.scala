package com.example.policemanthiefgame.services

import NetGraphAlgebraDefs.{Action, NodeObject}
import com.google.common.graph.MutableValueGraph
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Random

object GameLogic {

  private val logger = LoggerFactory.getLogger(getClass)

  def randomNode(graph: MutableValueGraph[NodeObject, _]): NodeObject = {
    val nodesList = graph.nodes().asScala.toList
    nodesList(Random.nextInt(nodesList.size))
  }
  
  def farthestNodeFrom(startNode: NodeObject, graph: MutableValueGraph[NodeObject, _]): NodeObject = {
    val visited: mutable.Set[NodeObject] = mutable.Set[NodeObject]()
    val queue: mutable.Queue[(NodeObject, Int)] = mutable.Queue[(NodeObject, Int)]()
    val distantNodes: mutable.Buffer[NodeObject] = mutable.Buffer[NodeObject]()

    queue.enqueue((startNode, 0))
    visited.add(startNode)

    while (queue.nonEmpty) {
      val (currentNode, distance) = queue.dequeue()
      // Add nodes that are at a distance (e.g., greater than a certain threshold)
      if (distance >= 3) { // Adjust the threshold as needed
        distantNodes += currentNode
      }

      val successors: Iterable[NodeObject] = graph.successors(currentNode).asScala
      for (successor <- successors) {
        if (!visited.contains(successor)) {
          visited.add(successor)
          queue.enqueue((successor, distance + 1))
        }
      }
    }

    if (distantNodes.nonEmpty) {
      // Randomly select one of the distant nodes
      distantNodes(Random.nextInt(distantNodes.size))
    } else {
      startNode // Fallback if no distant nodes are found
    }
  }

  def calculateConfidenceScore(perturbedNode: NodeObject, originalNode: NodeObject): Double = {
    logger.info("calculating confidence score")
    logger.info(s"perturbedNode Object ${perturbedNode}, originalNode Object ${originalNode}")

    val attributeComparisons = Seq(
      if (perturbedNode.children == originalNode.children) 1.0 else 0.0,
      if (perturbedNode.props == originalNode.props) 1.0 else 0.0,
      if (perturbedNode.currentDepth == originalNode.currentDepth) 1.0 else 0.0,
      if (perturbedNode.propValueRange == originalNode.propValueRange) 1.0 else 0.0,
      if (perturbedNode.maxDepth == originalNode.maxDepth) 1.0 else 0.0,
      if (perturbedNode.maxBranchingFactor == originalNode.maxBranchingFactor) 1.0 else 0.0,
      if (perturbedNode.maxProperties == originalNode.maxProperties) 1.0 else 0.0,
      if (perturbedNode.valuableData == originalNode.valuableData) 1.0 else 0.0,
      1.0 - math.abs(perturbedNode.storedValue - originalNode.storedValue)
    )

    attributeComparisons.sum / attributeComparisons.length
  }

  def calculateNextMove(currentNode: NodeObject, perturbedGraph: MutableValueGraph[NodeObject, Action], originalGraph: MutableValueGraph[NodeObject, Action]): Option[NodeObject] = {
    // Get adjacent nodes in perturbed and original graphs
    val perturbedSuccessors = perturbedGraph.successors(currentNode).asScala
//    val originalSuccessors = originalGraph.successors(currentNode).asScala.toSet

    // Find the corresponding node in the original graph based on ID and get its successors
    val originalSuccessors = originalGraph.nodes().asScala.find(_.id == currentNode.id)
      .map(node => originalGraph.successors(node).asScala.toSet)
      .getOrElse(Set.empty)

    // Calculate confidence scores for each perturbed adjacent node based on their counterparts in the original graph
    val scores = perturbedSuccessors.flatMap { perturbedNode =>
      originalSuccessors.find(_.id == perturbedNode.id)
        .map(originalNode => (perturbedNode, calculateConfidenceScore(perturbedNode, originalNode)))
    }

    // Find the maximum score
    val maxScore = scores.map(_._2).maxOption.getOrElse(Double.MinValue)

    val maxScoreNodes: Seq[NodeObject] = scores.filter(_._2 == maxScore).map(_._1).toSeq

    // If there are multiple nodes with the maximum score, randomly select one.
    // If there's only one, it will be the one selected.
    if (maxScoreNodes.nonEmpty) {
      Some(maxScoreNodes(Random.nextInt(maxScoreNodes.size)))
    } else {
      None // No adjacent nodes found, return None
    }
  }
}