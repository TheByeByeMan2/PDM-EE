package BattleShipApp.utils

import BattleShipApp.domain.Game
import BattleShipApp.domain.InputUserIdAndUserState
import BattleShipApp.domain.UserState
import BattleShipApp.errors.GameFinishException
import BattleShipApp.errors.SemanticErrorException
import BattleShipApp.repository.*
import BattleShipApp.transactions.Transaction

fun noNegative(i: Int, s: String){
    if (i <= 0) throw SemanticErrorException("The $s cannot be negative")
}

fun noEmptyString(s1: String, s2: String){
    if (s1.isBlank()) throw SemanticErrorException("The $s2 cannot be empty or full empty spaces")
}


fun checkUserById(transaction: Transaction, userId: Int){
    val rep = UsersRepository()
    if (!rep.checkIfUserIdExists(transaction, userId)) throw SemanticErrorException("The user not exist")
}

fun checkRuleById(transaction: Transaction, ruleId: Int){
    val rep = RulesRepository()
    if (!rep.checkIfRuleIdExists(transaction, ruleId)) throw SemanticErrorException("The rule not exist")
}

fun checkRuleGridSize(gridSize: String){
    val split = gridSize.split('x')
    if (split[0].toIntOrNull() == null || split[1].toIntOrNull() == null){
        throw SemanticErrorException("Invalid gridSize")
    } else {
        noNegative(split[0].toInt(), "grid size")
        noNegative(split[1].toInt(), "grid size")
    }
}

fun checkGameById(transaction: Transaction, gameId: Int){
    val rep = GamesRepository()
    if (!rep.checkIfGameIdExists(transaction, gameId)) throw SemanticErrorException("The game not exist")
}

fun checkAllAreFinish(transaction: Transaction, gameId: Int): Boolean{
    val rep = GamesRepository()
    val game = rep.getGameById(transaction, gameId)
    return game.finishA && game.finishB
}

fun checkAllShipArePut(transaction: Transaction, userId: Int){
    val rep = GameGridFleetRepository()
    val fleets = rep.getGridFleetsById(transaction, userId)
    if (fleets.find { it.quantity != 0 } != null) throw IllegalStateException("Not all ships were put")
}

fun checkShipByNameAndGameId(transaction: Transaction, shipName: String, gameId: Int){
    val rep = ShipTypeRepository()
    val rep2 = GamesRepository()
    val ruleId = rep2.getGameById(transaction, gameId).ruleId
    if(!rep.checkIfGameIdExists(transaction, ruleId, shipName)) throw SemanticErrorException("The ship with ruleId $ruleId not exist")
}

fun checkDirection(direction: String) {
    if (direction.toDirectionOrNull() == null) throw SemanticErrorException("The direction not exist")
}

fun illegalGameState(gameState: GameState):String{
    return when(gameState){
        GameState.START -> "you need building board"
        GameState.BATTLE -> "you need battle with other"
        GameState.END -> "the game is finish you need go"
    }
}

fun checkGameState(transaction: Transaction, gameId: Int, gameState: GameState){
    val rep = GamesRepository()
    val gameGameState = rep.getGameById(transaction, gameId).gameState
    val msg = illegalGameState(gameGameState)
    if (gameGameState !== gameState) throw IllegalStateException("Cannot do it now, $msg")
}

fun checkStartGameState(transaction: Transaction, gameId: Int){
    checkGameState(transaction, gameId, GameState.START)
}

fun checkBattleGameSate(transaction: Transaction, gameId: Int){
    checkGameState(transaction, gameId, GameState.BATTLE)
}

fun checkEndGameSate(transaction: Transaction, gameId: Int){
    val rep = GamesRepository()
    val game = rep.getGameById(transaction, gameId)
    val msg = illegalGameState(GameState.END)
    if (!(game.finishA || game.finishB || game.gameState === GameState.END)) throw IllegalStateException("Cannot do it now, $msg")
}

fun checkUserInGame(transaction: Transaction, gameId: Int, userId: Int){
    val rep = GamesRepository()
    val gameInfo = rep.getGameById(transaction, gameId)
    if (gameInfo.userA != userId && gameInfo.userB != userId) throw IllegalStateException("Error user in game")
}

private fun illegalUserState(userState: UserState):String{
    return when(userState){
        UserState.BATTLE -> "You are in game"
        UserState.WAITING -> "you are match making"
        UserState.FREE -> "you are free"
    }
}

/**
 *
 * throw exception when user not match current state
 */
fun checkUserState(transaction: Transaction, userId: Int, userState: UserState){
    val rep = UsersRepository()
    val user = rep.getUserById(transaction, userId)
    val msg = illegalUserState(userState)
    if (user.userState !== userState) throw IllegalStateException("Cannot do it now, $msg")
}

fun checkUserFreeState(transaction: Transaction, userId: Int){
    checkUserState(transaction, userId, UserState.FREE)
}
fun checkUserBattleState(transaction: Transaction, userId: Int){
    checkUserState(transaction, userId, UserState.BATTLE)
}
fun checkUserWaitingState(transaction: Transaction, userId: Int){
    checkUserState(transaction, userId, UserState.WAITING)
}

fun checkUserWaitingOrBattleState(transaction: Transaction, userId: Int){
    val rep = UsersRepository()
    val user = rep.getUserById(transaction, userId)
    if (user.userState !== UserState.WAITING && user.userState !== UserState.BATTLE) throw IllegalStateException("Cannot do it now")
}

fun checkWaitingRoomGameValueIsNull(transaction: Transaction, userId: Int):Boolean{
    val rep = WaitingRoomRepository()
    val waitingRoom = rep.getWaitingRoomByUser(transaction, userId)
    return waitingRoom.gameId == null
}

fun checkUserInWaitingRoom(transaction: Transaction, userId: Int){
    val rep = WaitingRoomRepository()
    val waitingRoom = rep.getWaitingRoomOrNullByUser(transaction, userId)
    if (waitingRoom != null) throw IllegalStateException("You are in waiting room")
}

fun inconvenientWinner(transaction: Transaction, gameId: Int){
    val rep = GamesRepository()
    if (rep.getGameById(transaction, gameId).winner != null) throw GameFinishException("The game is Finish")
}
