package com.example.policemanthiefgame.utils

import NetGraphAlgebraDefs.{Action, NodeObject}
import com.google.common.graph.{MutableValueGraph, ValueGraphBuilder}
import org.slf4j.LoggerFactory
import utils.NetGraph

object GraphLoader {
  private val logger = LoggerFactory.getLogger(getClass)

  def loadGraph(filePath: String): MutableValueGraph[NodeObject, Action] = {
    // Load the graph components using your existing logic
    val (nodes, edges) = NetGraph.loadGraph(filePath)
    logger.info(s"nodes : ${nodes}")
    logger.info(s"edges : ${edges}")

    val valueGraph: MutableValueGraph[NodeObject, Action] = ValueGraphBuilder.directed().build()
    // Add nodes to the graph
    nodes.foreach(valueGraph.addNode)

    edges.foreach { action =>
      val nodeFromOption: Option[NodeObject] = nodes.find(_.id == action.fromNode.id)
      val nodeToOption: Option[NodeObject] = nodes.find(_.id == action.toNode.id)

      nodeFromOption.flatMap { nodeFrom =>
        nodeToOption.map { nodeTo =>
          valueGraph.putEdgeValue(nodeFrom, nodeTo, action)
        }
      }
    }

    valueGraph
  }
}

