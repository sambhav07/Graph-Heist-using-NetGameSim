package utils

import NetGraphAlgebraDefs.{Action, NetGraphComponent, NodeObject}

import java.io.{FileInputStream, InputStream, ObjectInputStream}
import java.net.URL
import scala.util.Try

object NetGraph {
  def loadGraph(filePath: String): (List[NodeObject], List[Action]) = {
    val inputStream: Option[InputStream] = if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
      println("Inside s3 bucket check")
      Try(new URL(filePath).openStream()).toOption
    } else {
      Try(new FileInputStream(filePath)).toOption
    }
    inputStream match {
      case Some(stream) =>
        val objectInputStream = new ObjectInputStream(stream)
        val ng = objectInputStream.readObject().asInstanceOf[List[NetGraphComponent]]
        val nodes = ng.collect { case node: NodeObject => node }
        val edges = ng.collect { case edge: Action => edge }
        objectInputStream.close() // Close the stream after use
        (nodes, edges)
      case None =>
        throw new IllegalArgumentException("Invalid file path or URL")
    }
  }
}