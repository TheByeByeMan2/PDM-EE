package BattleShipApp.service

import BattleShipApp.controllers.CreateRuleAndShipTypes
import BattleShipApp.controllers.PutShips
import BattleShipApp.domain.*
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.utils.toUsernameAndPass
import org.springframework.stereotype.Component


@Component
class Services(
    private val dataBase: Data,
    private val rep: Repositories = Repositories(),
    private val initialTurnTimeControl: InitialTurnTimeControl = InitialTurnTimeControl(dataBase, rep),
    private val gameTimerControl: GameTimerControl = GameTimerControl(initialTurnTimeControl),
    val userServices: UserServices = UserServices(dataBase,rep ),
    val gameServices: GameServices = GameServices(dataBase, rep,
        gameTimerControl
    ),
    val gridServices: GridServices = GridServices(dataBase, rep),
    val ruleServices: RuleServices = RuleServices(dataBase, rep),
    val waitingRoomServices: WaitingRoomServices = WaitingRoomServices(dataBase, rep),
    val someLogic: MoreThanOneServices = MoreThanOneServices(dataBase, rep,
        gameTimerControl
    ),
    ) {

    fun getUsersRanking(limit: Int) = userServices.getUsersDetailsListTrans(limit)

    fun register(register: Register, token: String) = userServices.userRegisterTrans(register.toUsernameAndPass(), token)

    fun login(usernameAndPass: UsernameAndPass) = userServices.userLoginTrans(usernameAndPass)

    fun getUserComplexInfo(userId: Int) = someLogic.getUserComplexInfoTrans(userId)

    fun getUserInfo(userId: Int) = userServices.getUserDetailsByIdTrans(userId)

    fun matchMaking(userIdAndRule: JoinWaitingRoomInfo) = someLogic.userWaitingGameTrans(userIdAndRule)

    fun searchGame(userId: Int) = someLogic.waitInWaitingRoomUserTrans(userId)

    fun exitWaiting(userId: Int) = someLogic.exitWaitingRoomTrans(userId)

    fun putShip(inputGrid: InputGrid, direction: String) = gridServices.createNewGridTrans(inputGrid, direction)

    fun putShips(inputShip: PutShips) = gridServices.createPutShipsTrans(inputShip)

    fun removeShip(removeInfo: InputGridNonShipName) = gridServices.removeShipTrans(removeInfo)

    fun getPlayerGrid(gameIdAndUser: InputGetGridByGameIdAndUserId) = gridServices.getGridInfoTrans(gameIdAndUser)

    fun getGame(gameId: Int, userId: Int) = gameServices.getGameInfoByIdTrans(gameId, userId)

    fun setReady(gameId: Int, userId: Int) = gameServices.setGameReadyTrans(gameId, userId)

    fun shot(shotInfo: InputGridNonShipName): List<GridCell> = someLogic.shotGridTrans(shotInfo)

    fun gameEnd(gameId: Int, userId: Int) = someLogic.endGameTrans(gameId, userId)

    fun exitGame(gameId: Int, userId: Int) = someLogic.exitGameTrans(userId, gameId)

    fun getGrid(gameIdAndUser: InputGetGridByGameIdAndUserId) = gridServices.getGridInfoTrans(gameIdAndUser)

    fun getAllRules() = ruleServices.getAllRulesTrans()

    fun createRuleAndShip(inputRuleAngShip: CreateRuleAndShipTypes) = someLogic.createRuleAndShipTrans(inputRuleAngShip)

    fun getRuleInfo(ruleId: Int) = someLogic.getRuleAndShipTypeTrans(ruleId)

    fun timeover (timeoverInfo: InputUserIdGameId) = someLogic.timeover(timeoverInfo)
}