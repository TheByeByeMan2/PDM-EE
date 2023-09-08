package BattleShipApp.controllers

import BattleShipApp.domain.*
import BattleShipApp.service.Services
import BattleShipApp.transactions.TypeDB
import BattleShipApp.utils.*
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class Problem(val theProblem: String?)

@RestController
class Controllers {
    private final val typeDB = TypeDB()
    private val services = Services(typeDB)


    //Author and version information
    @GetMapping(Uris.AUTHORS)
    fun authorsInfo(): Authors {
        return createMyAuthors()
    }

    //Ranking information
    @GetMapping(Uris.RANKING)
    fun usersRanking(): ResponseEntity<*> {
        return try {
            val res = services.getUsersRanking(50)
            ResponseEntity.status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    //Register a new user
    @PostMapping(Uris.REGISTER)
    fun registerUser(@RequestBody register: Register): ResponseEntity<*> {
        return try {
            val token = newToken().toString()
            val res = services.register(register, token)
            ResponseEntity.status(201)
                .header("Authorization", token.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.LOGIN)
    fun loginUser(@RequestBody usernameAndPass: UsernameAndPass): ResponseEntity<*> {
        return try {
            val userAndToken = services.login(usernameAndPass)
            ResponseEntity
                .status(200)
                .header("Authorization", userAndToken.token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(userAndToken.userId))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @GetMapping(Uris.USER_COMPLEX_INFO)
    fun userState(@PathVariable id: Int, request: HttpServletRequest): ResponseEntity<*>{
        return try {
            if (userTokenCheck(request, id)) return badRequest()
            val userState = services.getUserComplexInfo(id)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(userState))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @GetMapping(Uris.USER_INFO)
    fun userInfo(@PathVariable id: Int, request: HttpServletRequest): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, id)) return badRequest()
            val userInfo = services.getUserInfo(id)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(userInfo))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    //start a new game
    @PostMapping(Uris.WAITING)
    fun createWaitingUser(
        @RequestBody userIdAndRule: JoinWaitingRoomInfo,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userIdAndRule.userId)) return badRequest()
            val res = services.matchMaking(userIdAndRule)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @GetMapping(Uris.WAITING)
    fun waitingRoomInfo(
        /*@RequestBody userId: UserId,*/
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val res = services.searchGame(userId)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.WAITING_EXIT)
    fun waitingRoomExit(@RequestBody userId: UserId, request: HttpServletRequest): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId.userId)) return badRequest()
            val res = services.exitWaiting(userId.userId)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }


    //the layout of their fleet in the grid
    @PostMapping(Uris.BUILDING)
    fun usersBuildingGrid(
        @PathVariable id: Int,
        @RequestBody userPutOrRemoveShip: UserPutOrRemoveShip,
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userPutOrRemoveShip.userId)) return badRequest()
            checkPut(userPutOrRemoveShip.shipName, userPutOrRemoveShip.action)
            val res = if (userPutOrRemoveShip.action.toPutRemove() === PutRemove.PUT) services.putShip(
                InputGrid(
                    id,
                    userPutOrRemoveShip.userId,
                    userPutOrRemoveShip.position.col,
                    userPutOrRemoveShip.position.row,
                    userPutOrRemoveShip.shipName!!,
                ),
                userPutOrRemoveShip.direction
            )
            else services.removeShip(
                InputGridNonShipName(
                    id,
                    userPutOrRemoveShip.userId,
                    userPutOrRemoveShip.position.col,
                    userPutOrRemoveShip.position.row,
                )
            )
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.BUILDING_ALL)
    fun usersPutAllGrid(
        @PathVariable id: Int,
        @RequestBody putShips: PutShips,
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, putShips.userId)) return badRequest()
            val res = services.putShips(putShips)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @GetMapping(Uris.BUILDING)
    fun getUserBuildingGridInfo(
        @PathVariable id: Int,
        //@RequestBody userId: UserId,
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val gridInfo = InputGetGridByGameIdAndUserId(id, userId)
            val res = services.getPlayerGrid(gridInfo)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    //get game Info
    @GetMapping(Uris.SET_READY)
    fun getUserGameInfo(
        @PathVariable id: Int,
        //@RequestBody userId: UserId,
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val res = services.getGame(id, userId)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.SET_READY)
    fun setGameReady(
        @PathVariable id: Int,
        @RequestBody userId: UserId,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId.userId)) return badRequest()
            val res = services.setReady(id, userId.userId)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.BATTLE)
    fun shotShipOnBoard(
        @PathVariable id: Int,
        @RequestBody userIdAndPosition: UserIdAndPosition,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userIdAndPosition.userId)) return badRequest()
            val grid: List<GridCell> = services.shot(
                InputGridNonShipName(
                    id,
                    userIdAndPosition.userId,
                    userIdAndPosition.position.col,
                    userIdAndPosition.position.row,
                )
            )
            val res = if (grid.isEmpty()) ShotResult(ShotState.MISS, null)
            else ShotResult(ShotState.SHOT, grid)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.END)
    fun finishingGame(
        @PathVariable id: Int,
        @RequestBody userId: UserId,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId.userId)) return badRequest()
            val res = services.gameEnd(id, userId.userId)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.BATTLE_EXIT) //TODO("Test when the game is end")
    fun exitGame(
        @PathVariable id: Int,
        @RequestBody userId: UserId,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId.userId)) return badRequest()
            val res = services.exitGame(id, userId.userId)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    //the state of fleet.
    @GetMapping(Uris.BATTLE)
    fun startGame(
        @PathVariable id: Int,
        //@RequestBody userId: UserId,
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val gridInfo = InputGetGridByGameIdAndUserId(id, userId)
            val res = services.getGrid(gridInfo)
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    //state of a game
    @GetMapping(Uris.GAME_INFO)
    fun getGameInfo(
        @PathVariable id: Int,
        //@RequestBody userId: UserId,
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val res = services.getGame(id, userId)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @GetMapping(Uris.ALL_RULES)
    fun getAllRules(
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val res = services.getAllRules()
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @PostMapping(Uris.ALL_RULES)
    fun createRule(
        @RequestHeader userId: Int,
        @RequestBody inputRuleAngShip: CreateRuleAndShipTypes,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val res = services.createRuleAndShip(inputRuleAngShip)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }

    @GetMapping(Uris.RULE)
    fun getRuleInfo(
        @PathVariable ruleId: Int,
        @RequestHeader userId: Int,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, userId)) return badRequest()
            val res = services.getRuleInfo(ruleId)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(res))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }

    }

    @PostMapping("api/time")
    fun timeover(@RequestBody timeoverInfo: InputUserIdGameId, request: HttpServletRequest): ResponseEntity<*> {
        return try {
            if (userTokenCheck(request, timeoverInfo.userId)) return badRequest()
            println("início controller")
            services.timeover(timeoverInfo)
            println("fim controller")
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JacksonConv.classToJson(TimeoverRes(true)))
        } catch (e: Exception) {
            ResponseEntity
                .status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Problem(e.message))
        }
    }
}
