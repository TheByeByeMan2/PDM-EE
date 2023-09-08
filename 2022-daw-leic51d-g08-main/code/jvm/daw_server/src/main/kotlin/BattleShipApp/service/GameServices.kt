package BattleShipApp.service

import BattleShipApp.domain.*
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import BattleShipApp.utils.*
import OutPutDeleteGridCell
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GameServices(
    private val dataBase: Data,
    private val rep: Repositories,
    private val gameTimerControl: GameTimerControl
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createNewGame(transaction: Transaction, usersAndRule: InputCreateGame): Game {
        return rep.gamesRep.createGame(transaction, usersAndRule)
    }


    private fun getGameInfoById(transaction: Transaction, gameId: Int): Game {
        return rep.gamesRep.getGameById(transaction, gameId)
    }

    fun getGameInfoByIdTrans(gameId: Int, userId: Int): Game {
        val trans = dataBase.createTransaction()
        noNegative(gameId, "game id")
        return trans.executeTransaction<Game>(trans) {
            checkUserBattleState(it, userId)
            checkGameById(it, gameId)
            getGameInfoById(it, gameId)
        }
    }
    fun setGameWinner(transaction: Transaction, gameWinnerInput: InputGameWinner): Game {
        return rep.gamesRep.setGameWinner(transaction, gameWinnerInput)
    }

    private fun setGameReady(transaction: Transaction, gameIdAndUserId: InputUserIdGameId): Game {
        return rep.gamesRep.setGameReady(transaction, gameIdAndUserId)
    }

    /**
     *
     * return true when the game ready a and b are true
     */
    private fun checkGameIsReady(transaction: Transaction, gameId: Int): Boolean {
        val gameInfo = getGameInfoById(transaction, gameId)
        return gameInfo.readyA && gameInfo.readyB
    }

    /**
     * Update game ready to true
     * and check if the game readies are true
     * if it is true update game timeout
     */
    fun setGameReadyTrans(gameId: Int, userId: Int): Game {
        val trans = dataBase.createTransaction()
        noNegative(gameId, "game id")
        return trans.executeTransaction(trans) {
            inconvenientWinner(it, gameId)
            checkUserBattleState(it, userId)
            checkStartGameState(it, gameId)
            checkUserInGame(it, gameId,userId)
            checkGameById(it, gameId)
            checkAllShipArePut(it, userId)
            val res = setGameReady(it, InputUserIdGameId(userId, gameId))
            if (checkGameIsReady(it, gameId)) {
                updateGameState(it, InputUpdateGameState(gameId, GameState.BATTLE))
                gameTimerControl.startTimer(gameId)
            }
            res
        }
    }

    fun endGame(transaction: Transaction, gameIdAndUserId: InputUserIdGameId): Game{
        return rep.gamesRep.setGameFinish(transaction, gameIdAndUserId)
    }

    fun deleteGame(transaction: Transaction, gameId: Int): Boolean = rep.gamesRep.deleteGame(transaction,gameId)

    /** caso especial **/
    fun deleteGrid(transaction: Transaction,gameId: Int, userId: Int): List<OutPutDeleteGridCell>{
        val gridInfo = rep.gridRep.getGridInfo(transaction, InputGetGridByGameIdAndUserId(gameId, userId))
        val inputList = gridInfo.map {grid-> InputGridNonShipName(grid.gameId, grid.userId
            , grid.column, grid.row) }
        return rep.gridRep.deleteGrid(transaction, inputList)
    }

    fun deleteGridTrans(gameId: Int, userId: Int): List<OutPutDeleteGridCell>{
        val trans = dataBase.createTransaction()
        return trans.executeTransaction<List<OutPutDeleteGridCell>>(trans) {
            deleteGrid(it, gameId, userId)
        }
    }

    fun updateGameState(transaction: Transaction, gameIdAndGameState: InputUpdateGameState): Game{
        return rep.gamesRep.updateGameState(transaction, gameIdAndGameState)
    }

    fun findGameByUserId(transaction: Transaction, userId: Int): Game?{
        return rep.gamesRep.getGameByUserId(transaction, userId)
    }

}