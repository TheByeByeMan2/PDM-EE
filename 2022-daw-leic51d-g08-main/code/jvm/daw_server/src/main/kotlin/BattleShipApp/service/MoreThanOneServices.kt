package BattleShipApp.service

import BattleShipApp.controllers.CreateRuleAndShipTypes
import BattleShipApp.controllers.InputCreateShipType
import BattleShipApp.domain.*
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import BattleShipApp.utils.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MoreThanOneServices(
    private val dataBase: Data,
    private val rep: Repositories,
    private val gameTimerControl: GameTimerControl
) {
    private val gameServices = GameServices(
        dataBase, rep,
        gameTimerControl
    )
    private val gridServices = GridServices(dataBase, rep)
    private val userServices = UserServices(dataBase, rep)
    private val waitingRoomServices = WaitingRoomServices(dataBase, rep)
    private val gridFleetServices = GameGridFleetLogic(dataBase)
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private fun waitingRoomLogic(transaction: Transaction, usersAndRule: InputGameUsersAndRuleId): WaitingRoom? {
        val waitingRooms = rep.waitingRoomRep.getAllWaitingRoom(transaction)
        val otherUserId = usersAndRule.userIdB
        val userId = usersAndRule.userIdA
        return if (waitingRooms.size >= 2 && userId != otherUserId) {
            val perShot = rep.rulesRep.getRule(transaction, usersAndRule.ruleId).shotNumber
            val game = gameServices.createNewGame(
                transaction,
                InputCreateGame(otherUserId, userId, usersAndRule.ruleId, perShot)
            )
            val shipTypes = rep.shipTypeRep.getAllShipTypeByRuleId(transaction, usersAndRule.ruleId)
            val listInputGridFleet = mutableListOf<InputGridFleet>()
            shipTypes.forEach { listInputGridFleet.add(InputGridFleet(userId, it.shipName, it.fleetQuantity)) }
            userServices.updateUserState(transaction, InputUserIdAndUserState(userId, UserState.BATTLE))
            rep.gameGridFleetRep.createMultipleGridFleet(transaction, listInputGridFleet)
            waitingRoomServices.setWaitingRoomGameId(transaction, InputUserIdGameId(userId, game.gameId))
            waitingRoomServices.setWaitingRoomGameId(transaction, InputUserIdGameId(otherUserId, game.gameId))
            userServices.updateUserState(
                transaction,
                InputUserIdAndUserState(otherUserId, UserState.BATTLE)
            )
            waitingRoomServices.setWaitingRoomIsGo(transaction, InputUpdateWaitingRoomIsGo(userId, true))
        } else null

    }

    /**
     * Create a new waiting room if the first waiting room under the same rule does nothing
     * else create a new game and modify the game ID of my and each other's and
     * create a new game grid fleet
     */
    private fun userWaitingGameLogic(transaction: Transaction, userIdAndRule: JoinWaitingRoomInfo): WaitingRoom {
        val waitingRoom = rep.waitingRoomRep.createWaitingRoom(transaction, userIdAndRule)
        val waitingRooms = rep.waitingRoomRep.getAllWaitingRoom(transaction)
        val matchMaking = waitingRooms.filter { it.userId != userIdAndRule.userId && it.ruleId == userIdAndRule.ruleId }
        if (matchMaking.isNotEmpty()) {
            val otherUserId = matchMaking.first().userId
            return waitingRoomLogic(
                transaction,
                InputGameUsersAndRuleId(userIdAndRule.userId, otherUserId, userIdAndRule.ruleId)
            ) ?: waitingRoom
        }
        return waitingRoom
    }

    fun userWaitingGameTrans(userIdAndRule: JoinWaitingRoomInfo): WaitingRoom {
        val trans = dataBase.createTransaction()
        noNegative(userIdAndRule.userId, "user id")
        noNegative(userIdAndRule.ruleId, "rule id")
        return trans.executeTransaction<WaitingRoom>(trans) {
            checkUserInWaitingRoom(it, userIdAndRule.userId)
            checkUserFreeState(it, userIdAndRule.userId)
            userServices.updateUserState(it, InputUserIdAndUserState(userIdAndRule.userId, UserState.WAITING))
            checkUserById(trans, userIdAndRule.userId)
            checkRuleById(it, userIdAndRule.ruleId)
            userWaitingGameLogic(it, userIdAndRule)
        }
    }


    /**
     * get the waiting room information
     * if the game ID is different of null get this information, create game grid fleet
     * and delete my and each other's waiting room else
     * simply get this waiting room information
     */
    private fun getUserWaitingRoom(transaction: Transaction, userId: Int): WaitingRoom {
        val ruleId = rep.waitingRoomRep.getWaitingRoomByUser(transaction, userId).ruleId
        val list =
            rep.waitingRoomRep.getAllWaitingRoom(transaction).filter { it.userId != userId && it.ruleId == ruleId }
        return if (list.isNotEmpty()) {
            val otherUserId = list.first().userId
            verifierCreateAndDelete(transaction, userId, otherUserId, ruleId)
                ?: rep.waitingRoomRep.getWaitingRoomByUser(transaction, userId)
        } else rep.waitingRoomRep.getWaitingRoomByUser(transaction, userId)
    }


    fun waitInWaitingRoomUserTrans(userId: Int): WaitingRoom {
        val trans = dataBase.createTransaction()
        noNegative(userId, "user id")
        return trans.executeTransaction<WaitingRoom>(trans) {
            checkUserById(trans, userId)
            checkUserWaitingOrBattleState(it, userId)
            getUserWaitingRoom(it, userId)
        }
    }

    fun verifierCreateAndDelete(transaction: Transaction, userId: Int, otherUserId: Int, ruleId: Int): WaitingRoom? {
        val room = rep.waitingRoomRep.getWaitingRoomByUser(transaction, userId)
        return if (room.gameId != null && userId != otherUserId) {
            val res = waitingRoomServices.setWaitingRoomIsGo(transaction, InputUpdateWaitingRoomIsGo(userId, true))
            userServices.updateUserState(transaction, InputUserIdAndUserState(userId, UserState.BATTLE))
            rep.waitingRoomRep.deleteWaitingRoom(transaction, userId)
            rep.waitingRoomRep.deleteWaitingRoom(transaction, otherUserId)
            val shipTypes = rep.shipTypeRep.getAllShipTypeByRuleId(transaction, ruleId)
            val listInputGridFleet = mutableListOf<InputGridFleet>()
            shipTypes.forEach { listInputGridFleet.add(InputGridFleet(userId, it.shipName, it.fleetQuantity)) }
            rep.gameGridFleetRep.createMultipleGridFleet(transaction, listInputGridFleet)
            res
        } else null
    }

    fun shotGridTrans(input: InputGridNonShipName): List<GridCell> {
        val trans = dataBase.createTransaction()
        noNegative(input.userId, "user id")
        noNegative(input.col, "col")
        noNegative(input.row, "row")
        noNegative(input.gameId, "game id")
        return trans.executeTransaction(trans) {
            inconvenientWinner(it, input.gameId)
            checkUserById(it, input.userId)
            checkUserBattleState(it, input.userId)
            checkBattleGameSate(it, input.gameId)
            checkGameById(it, input.gameId)
            shotLogic(it, input)
        }
    }

    private fun shot(transaction: Transaction, input: InputGridNonShipName, rule: Rule): MutableList<GridCell> {
        val other = gridServices.getOtherPlayer(transaction, input.userId, input.gameId)
        val sinkShips = gridServices.sinkShip(transaction, input.copy(userId = other), rule)
        val otherGrid =
            rep.gridRep.getGridInfo(
                transaction,
                InputGetGridByGameIdAndUserId(
                    input.gameId,
                    gridServices.getOtherPlayer(transaction, input.userId, input.gameId)
                )
            )
        if (gridServices.checkWinner(otherGrid)) {
            val winnerName = rep.usersRep.getUserById(transaction, input.userId).username
            gameTimerControl.endTimer(input.gameId)
            gameServices.setGameWinner(transaction, InputGameWinner(input.gameId, winnerName))
            gameServices.updateGameState(transaction, InputUpdateGameState(input.gameId, GameState.END))
            rep.usersRep.updateUsersScoreAndGamePlayed(transaction, InputUsersAndScores(input.userId, other, 10, -5))
        }
        return sinkShips
    }


    private fun shotLogic(transaction: Transaction, input: InputGridNonShipName): List<GridCell> {
        try {
            var res = mutableListOf<GridCell>()
            val gameInfo = rep.gamesRep.getGameById(transaction, input.gameId)
            val rule = rep.rulesRep.getRule(transaction, gameInfo.ruleId)
            val cell = gridServices.shotGrid(transaction, input)
            gameTimerControl.updateTimerState(gameInfo.gameId, InitialTurnTimeControl.TimeState.INTERRUPTED)
            if (cell == null) {
                gridServices.miss(transaction, gameInfo.remainingShot, input.gameId, rule.shotNumber)
            } else {
                res = shot(transaction, input, rule)
            }
            if (rep.gamesRep.getGameById(
                    transaction,
                    input.gameId
                ).winner == null
            ) gameTimerControl.startTimer(gameInfo.gameId)
            return if (res.isEmpty() && cell != null) {
                res.add(cell)
                res
            } else res
        } catch (e: Exception) {
            throw e
        }
    }


    /**
     * delete player grid fleet and update game finish to true
     */
    fun endGameTrans(gameId: Int, userId: Int): Game {
        val trans = dataBase.createTransaction()
        noNegative(gameId, "game id")
        noNegative(userId, "user id")
        return trans.executeTransaction(trans) {
            checkGameById(it, gameId)
            checkUserBattleState(it, userId)
            checkEndGameSate(it, gameId)
            checkUserById(it, userId)
            val ruleId = rep.gamesRep.getGameById(it, gameId).ruleId
            val shipTypes = rep.shipTypeRep.getAllShipTypeByRuleId(it, ruleId)
            val ships = shipTypes.map { ship -> ship.shipName }
            gridFleetServices.deleteGridFleets(it, userId, ships)
            userServices.updateUserState(it, InputUserIdAndUserState(userId, UserState.FREE))
            gameServices.deleteGrid(it, gameId, userId)
            val res = gameServices.endGame(it, InputUserIdGameId(userId, gameId))
            if (checkAllAreFinish(it, gameId)) {
                gameServices.deleteGame(it, gameId)
            }
            res
        }
    }

    fun exitWaitingRoomTrans(userId: Int): Int {
        val trans = dataBase.createTransaction()
        noNegative(userId, "user id")
        return trans.executeTransaction(trans) {
            checkUserById(it, userId)
            checkWaitingRoomGameValueIsNull(it, userId)
            checkUserWaitingState(it, userId)
            exitWaitingRoom(it, userId)
        }
    }

    private fun exitWaitingRoom(transaction: Transaction, userId: Int): Int {
        val res = waitingRoomServices.exitWaitingRoom(transaction, userId)
        userServices.updateUserState(transaction, InputUserIdAndUserState(userId, UserState.FREE))
        return res
    }

    fun exitGameTrans(userId: Int, gameId: Int): Game {
        val trans = dataBase.createTransaction()
        noNegative(gameId, "game id")
        noNegative(userId, "user id")
        return trans.executeTransaction(trans) {
            checkGameById(it, gameId)
            checkUserById(it, userId)
            checkUserInGame(it, gameId, userId)
            checkUserBattleState(it, userId)
            exitGameLogic(it, userId, gameId)
        }
    }

    private fun exitGameLogic(transaction: Transaction, userId: Int, gameId: Int): Game {
        val game = rep.gamesRep.getGameById(transaction, gameId)
        val otherUserId = if (game.userA == userId) game.userB
        else game.userA
        val winnerName = rep.usersRep.getUserById(transaction, otherUserId).username
        if (game.gameState !== GameState.END) {
            gameServices.setGameWinner(transaction, InputGameWinner(gameId, winnerName))
            rep.usersRep.updateUsersScoreAndGamePlayed(transaction, InputUsersAndScores(otherUserId, userId, 5, -2))
        }
        gameServices.deleteGrid(transaction, gameId, userId)
        val ruleId = rep.gamesRep.getGameById(transaction, gameId).ruleId
        val shipTypes = rep.shipTypeRep.getAllShipTypeByRuleId(transaction, ruleId)
        val ships = shipTypes.map { ship -> ship.shipName }
        gridFleetServices.deleteGridFleets(transaction, userId, ships)
        val res = gameServices.endGame(transaction, InputUserIdGameId(userId, gameId))
        if (checkAllAreFinish(transaction, gameId)) {
            gameServices.deleteGame(transaction, gameId)
        }
        userServices.updateUserState(transaction, InputUserIdAndUserState(userId, UserState.FREE))
        return res
    }

    private fun createRuleAndShip(transaction: Transaction, ruleAndShipType: CreateRuleAndShipTypes): Int {
        val rule = rep.rulesRep.createRule(transaction, ruleAndShipType.rule)
        ruleAndShipType.shipTypes.forEach {
            createShip(transaction, rule.ruleId, it)
        }
        return rule.ruleId
    }

    private fun createShip(transaction: Transaction, ruleId: Int,shipType: InputCreateShipType): ShipType {
        return rep.shipTypeRep.createShipType(
            transaction,
            InputShipType(
                ruleId,
                shipType.shipName,
                shipType.squares,
                shipType.fleetQuantity
            )
        )
    }

    fun createRuleAndShipTrans(ruleAndShipType: CreateRuleAndShipTypes): Int{
        val trans = dataBase.createTransaction()
        noNegative(ruleAndShipType.rule.timeout, "timeout")
        noNegative(ruleAndShipType.rule.shotNumber, "shot number")
        noEmptyString(ruleAndShipType.rule.gridSize, "grid size")
        checkRuleGridSize(ruleAndShipType.rule.gridSize)
        return trans.executeTransaction(trans){
            createRuleAndShip(it, ruleAndShipType)
        }
    }

    private fun getRuleAndShipType(transaction: Transaction,ruleId: Int):RuleAndShipType{
        val rule = rep.rulesRep.getRule(transaction, ruleId)
        val shipTypes = rep.shipTypeRep.getAllShipTypeByRuleId(transaction, ruleId)
        return RuleAndShipType(rule, shipTypes)
    }

    fun getRuleAndShipTypeTrans(ruleId: Int):RuleAndShipType{
        val trans = dataBase.createTransaction()
        noNegative(ruleId, "rule Id")
        return trans.executeTransaction(trans){
            getRuleAndShipType(it, ruleId)
        }
    }

    fun getUserComplexInfoTrans(userId: Int):UserComplexInfo{
        noNegative(userId, "User id")
        val trans = dataBase.createTransaction()
        return trans.executeTransaction(trans){
            val user = userServices.getUserById(it, userId)
            val game = gameServices.findGameByUserId(it, userId)
            UserComplexInfo(user.userId,  user.userState, game?.gameId, game?.ruleId)
        }
    }

    fun timeover(timeover: InputUserIdGameId) {
        println("início ser")
        val trans = dataBase.createTransaction()
        return trans.executeTransaction(trans) {
            val other = gridServices.getOtherPlayer(it, timeover.userId, timeover.gameId)
            println(other)
            val winnerName = rep.usersRep.getUserById(it, other).username
            println(winnerName)
            gameTimerControl.endTimer(timeover.gameId)
            val a = gameServices.setGameWinner(it, InputGameWinner(timeover.gameId, winnerName))
            println(a)
            val b = gameServices.updateGameState(it, InputUpdateGameState(timeover.gameId, GameState.END))
            println(b)
            rep.usersRep.updateUsersScoreAndGamePlayed(it, InputUsersAndScores(timeover.userId, other, 10, -5))
            println("fim ser")
        }
    }
}