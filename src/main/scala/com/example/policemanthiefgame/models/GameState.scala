package com.example.policemanthiefgame.models

import NetGraphAlgebraDefs.NodeObject

// Game state
case class GameState(
                      thiefPosition: Option[NodeObject],
                      policePosition: Option[NodeObject],
                      gameOver: Boolean = false,
                      result: Option[String] = None,
                      lastPlayer: Option[String] = None
                    )

