package com.example.policemanthiefgame

import com.example.policemanthiefgame.api.WebServerGame

object Main extends App {
  // Start the server using the object that contains the 'main' method logic
  WebServerGame.start(args(0),args(1),args(2))
}
