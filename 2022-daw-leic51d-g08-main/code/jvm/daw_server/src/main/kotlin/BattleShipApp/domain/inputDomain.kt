package BattleShipApp.domain

import BattleShipApp.utils.GameState

/** USERS **/


/**
 * Gathers user info to increment or decrement score and inc games played
 * @param userIdA the id of the user a
 * @param userIdB the id of the user b
 * @param tScoreA the score of the user a
 * @param tScoreB the score of the user b
 */
data class InputUsersAndScores(
    val userIdA: Int,
    val userIdB: Int,
    val tScoreA: Int,
    val tScoreB: Int,
)

data class InputUserAndScore(
    val userId: Int,
    val score: Int,
)

/** GAMES **/


/**
 * Gathers gameId to set the winner
 * @param gameId the id of the game
 * @param winner the winning user
 */
data class InputGameWinner(
    val gameId: Int,
    val winner: String,
)

data class InputCreateGame(val userIdA: Int, val userIdB: Int, val ruleId: Int, val remainingShot: Int)

/**
 * Gathers games id to create or find gameId
 * @param userIdA the id of the user a
 * @param userIdB the id of the user b
 */
data class InputGameUsersAndRuleId(val userIdA: Int, val userIdB: Int, val ruleId: Int)

data class InputUpdatePerShot(val gameId: Int, val remainingShot: Int)

data class InputUpdateGameState(val gameId: Int, val gameState: GameState)

/** GRIDS **/

/**
 * Gathers grid info to create grid
 * @param gameId the id of the game
 * @param userId the id of the user
 * @param col the grid column
 * @param row the frid row
 */
data class InputGrid(val gameId: Int, val userId: Int, val col: Int, val row: Int, val shipName: String)

data class InputGridNonShipName(val gameId: Int, val userId: Int, val col: Int, val row: Int)

data class InputGetGridByGameIdAndUserId(val gameId: Int, val userId: Int)
/**
 * Gathers grid info to modifier the grid's ship state
 * @param gameId the id of the game
 * @param userId the id of the user
 * @param col the grid column
 * @param row the frid row
 * @param shipState the state of the ship to be used
 */
data class InputUpdateGridShipState(val gameId: Int, val userId: Int, val col: Int, val row: Int, val shipState: String)


data class InputUserIdGameId(val userId: Int,
                             val gameId: Int,)

data class InputUpdateWaitingRoomIsGo(val userId: Int,
                                      val isGo: Boolean,)

data class InputCreateRule(
    val gridSize : String,
    val shotNumber: Int,
    val timeout: Int,
)

data class InputGridFleet(
    val userId: Int,
    val shipName: String,
    val quantity: Int
)

data class JoinWaitingRoomInfo(
    val userId: Int,
    val ruleId: Int,
)

data class InputShipType(
    val ruleId: Int,
    val shipName: String,
    val squares: Int,
    val fleetQuantity: Int
)

data class InputGetShipType(
    val ruleId: Int,
    val shipName: String,
)

data class InputUserShipName(
    val userId: Int,
    val shipName: String,
)

data class InputUserToken(
    val userId: Int,
    val token: String,
)

data class InputUserIdAndUserState(
    val userId: Int,
    val userState: UserState,
)