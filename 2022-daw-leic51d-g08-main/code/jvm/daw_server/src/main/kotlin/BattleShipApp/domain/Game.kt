package BattleShipApp.domain

import BattleShipApp.utils.GameState
import java.sql.Timestamp

data class Game(
    val gameId: Int,
    val userA: Int,
    val userB: Int,
    val turn: Int,
    val initialTurn: Timestamp?,
    val readyA: Boolean,
    val readyB: Boolean,
    val winner: String?,
    val finishA: Boolean,
    val finishB: Boolean,
    val ruleId: Int,
    val remainingShot: Int,
    val gameState: GameState,
)

data class TimeoverRes(val res: Boolean)