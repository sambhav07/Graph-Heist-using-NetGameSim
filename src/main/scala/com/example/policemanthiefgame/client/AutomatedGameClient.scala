package com.example.policemanthiefgame.client
import org.slf4j.LoggerFactory
import scalaj.http.Http
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.util.Random

object AutomatedGameClient {
  private val serverUrl = "http://0.0.0.0:8080"
  private val numberOfGamesToPlay = 5
  private val logger = LoggerFactory.getLogger(AutomatedGameClient.getClass)

  def playGame(): Unit = {
//    resetReportFile()
    var gamesPlayed = 0
    val gameReport = new StringBuilder
      while (gamesPlayed < numberOfGamesToPlay) {
        var gameOver = false
        var thiefPositions = List[String]()
        var policePositions = List[String]()
        var lastPlayer = if (Random.nextBoolean()) "police" else "thief"

        logger.info(s"Starting game number: ${gamesPlayed + 1}")

        gameReport.append(s"Strategy: Confidence Score\nGame ${gamesPlayed + 1}\n")

        logger.info(s"gameOver ${gameOver}")
        while (!gameOver) {
          val currentPlayer = if (lastPlayer == "police") "thief" else "police"
          val response = makeMove(currentPlayer)
          logger.info(s"Response for $currentPlayer move: $response")

          // Extract positions from the response
          val positionOption = extractPosition(response, currentPlayer)
          logger.info(s"position and current player: ${positionOption} ${currentPlayer}")
          positionOption match {
            case Some(position) =>
              if (currentPlayer == "thief") {
                thiefPositions ::= position // Prepend position to thiefPositions
              } else {
                policePositions ::= position // Prepend position to policePositions
              }
            case None => // Do nothing if position is not found
          }

          if (response.contains("Game over")) {
            gameOver = true
            logger.info("inside Game Over")
            logger.info(response) // Log the game-over message
            logger.info("Game over detected. Breaking out of the game loop.")
            gameReport.append(s"Thief Moves: ${thiefPositions.reverse.mkString(", ")}\n")
            gameReport.append(s"Police Moves: ${policePositions.reverse.mkString(", ")}\n")
            gameReport.append(s"Game Result: $response\n")
            resetGame()
            gameReport.append("Game Reset: true\n----------\n")
            gamesPlayed += 1
          } else {
            logger.info("Continuing the game...")
            lastPlayer = currentPlayer
          }

          logger.info("Exited the game loop.")
          // Pause between moves
          logger.info("before thread")
          Thread.sleep(1000)
        }
      }
    appendToFile(gameReport.toString())
      println(s"Finished playing $numberOfGamesToPlay games.")
  }

  private def appendToFile(content: String): Unit = {
//    val reportPath = "s3://policethiefgame/output/report.txt"
//    val reportPath = "/home/ubuntu/myapp/report.txt"
    val reportPath = "/Users/sambhavjain/Desktop/newrepo/Policeman_Thief_Graph_Game/outputs/report.txt"

    if (reportPath.startsWith("s3://")) {
      val (bucket, key) = parseS3Path(reportPath)
      println(s"bucket key value ${bucket} ${key}")
      writeToS3(bucket, key, content)
    } else {
      writeToLocalFile(content, reportPath)
    }
  }

  private def parseS3Path(s3Path: String): (String, String) = {
    val path = s3Path.stripPrefix("s3://")
    val parts = path.split("/", 2)
    val bucket = parts(0)
    val key = if (parts.length > 1) parts(1) else "report.txt"
    (bucket, key)
  }
  private def writeToS3(bucket: String, key: String, content: String): Unit = {
    println(s"content to be written ${content}")
    logger.info(s"Inside writeToS3")
    val s3Client = S3Client.builder()
      .region(Region.US_EAST_1) // Or your preferred region
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIATJ7MSUGHDGZSJH52", "pFnuAFFB9jGN6njE3uct71hNNzmNSsWpbnZsvKm1")))
      .build()
    try{
    s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromString(content))}
    catch {
      case e: Exception => logger.error("Error while writing to S3", e)
    } finally {
      s3Client.close()
      logger.info("S3 client closed")
    }
  }

  private def writeToLocalFile(content: String, filePath: String): Unit = {
    val path = Paths.get(filePath)
    Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }

  private def makeMove(playerType: String): String = {
    val response = Http(s"$serverUrl/move/$playerType").asString
    response.body
  }

  private def resetGame(): Unit = {
    val response = Http(s"$serverUrl/reset").asString
    println("Game reset response: " + response.body)
  }

  private def extractPosition(response: String, playerType: String): Option[String] = {
    response match {
      case r if r.startsWith("GameState") =>
        val gameStatePattern = raw"Thief Position: (\d+|Unknown), Police Position: (\d+|Unknown)".r
        gameStatePattern.findFirstMatchIn(response).flatMap { matchResult =>
          playerType match {
            case "thief" => Option(matchResult.group(1))
            case "police" => Option(matchResult.group(2))
          }
        }

      case r if r.contains("Game over") =>
        val gameOverPattern = raw".*?node (\d+).*Game over.*".r
        gameOverPattern.findFirstMatchIn(response).map(_.group(1))

      case _ => None // No position information found
    }
  }

  private def resetReportFile(): Unit = {
    val path = Paths.get("/Users/sambhavjain/Desktop/newrepo/Policeman_Thief_Graph_Game/outputs/report.txt")
    try {
      Files.deleteIfExists(path)
      logger.info("Existing report file deleted.")
    } catch {
      case e: Exception => logger.error("Failed to delete the existing report file", e)
    }
  }
}
