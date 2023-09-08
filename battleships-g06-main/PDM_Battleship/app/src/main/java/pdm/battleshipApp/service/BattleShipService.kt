package pdm.battleshipApp.service


import androidx.compose.ui.text.toLowerCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.domain.*
import pdm.battleshipApp.exceptions.*
import pdm.battleshipApp.utils.JacksonConv
import pdm.battleshipApp.utils.send
import pdm.battleshipApp.utils.toServerPosition
import pdm.battleshipApp.utils.toSupportPosition
import java.net.URL


const val PROTOCOL = "http"

const val ISEL = "194.210.199.32"
const val HOME = "10.0.2.2"
const val HOST = HOME
const val PORT = 8080

const val RULE_ID = 2
const val API = "api/"
const val RANKING = "${API}ranking"
const val AUTHORS = "${API}authors"
const val LOGIN = "${API}login"
const val USER_INFO = "${API}user"
const val REGISTER = "${API}register"
const val WAITING_ROOM = "${API}games/waiting"
const val WAITING_ROOM_EXIT = "${API}games/waiting/exit"
const val WAITING_ROOM_LISTENER = "${API}games/waiting/chat"
fun COMPLEXT_USER_INFO(userId: Int) = "${API}/user/special/$userId"
fun GAME_BUILDING(gameId: Int) = "${API}games/$gameId/building"
fun GAME_BUILDING_ALL(gameId: Int) = "${API}games/$gameId/building_all"
fun SET_READY(gameId: Int) = "${API}games/$gameId/isReady"
fun GAME_BATTLE(gameId: Int) = "${API}games/$gameId/battle"
fun EXIT_GAME(gameId: Int) = "${API}games/$gameId/exit"
fun GAME_INFO(gameId: Int) = "${API}games/$gameId/info"
fun END_GAME(gameId: Int) = "${API}games/$gameId/end"

class BattleShipService(
    private val httpClient: OkHttpClient,
    private val listenerClient: OkHttpClient
) : Service {

    override suspend fun fetchRanking(mode: Service.Mode): List<UserDetail> {
        val request = buildRequest(url = URL(PROTOCOL, HOST, PORT, RANKING), method = "GET")
        return request.send(httpClient) { response ->
            handleResponse(response, Array<UserDetail>::class.java).toList()
        }
    }

    override suspend fun fetchAuthors(): Authors {
        val request = buildRequest(url = URL(PROTOCOL, HOST, PORT, AUTHORS), method = "GET")
        return request.send(httpClient) { response ->
            handleResponse(response, Authors::class.java)
        }
    }

    override suspend fun login(username: String, pass: String): UserIdAndToken {
        val json = JacksonConv.classToJson(UsernameAndPass(username, pass))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request =
            buildRequest(url = URL(PROTOCOL, HOST, PORT, LOGIN), method = "POST", body = body)
        return request.send(httpClient) { response ->
            handleLoginResponse(response)
        }
    }

    override suspend fun fetchUserInfo(userId: Int, token: String): UserDetail {
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, "$USER_INFO/$userId"),
            method = "GET",
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        return request.send(httpClient) { response ->
            handleResponse(response, UserDetail::class.java)
        }
    }

    override suspend fun register(username: String, pass: String): User {
        val json = JacksonConv.classToJson(UsernameAndPass(username, pass))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, REGISTER),
            method = "POST",
            body = body
        )
        val support = request.send(httpClient) { response ->
            handleResponse(response, UserSupport::class.java, RegisterServerException())
        }
        return User(support.userId, support.username, support.userState)
    }

    override fun waitingRoomListener(): Flow<WaitingRoomMsg> {
        return callbackFlow {
            try {
                val request = buildRequest(
                    url = URL(PROTOCOL, HOST, PORT, WAITING_ROOM_LISTENER),
                    method = "GET"
                )
                request.send(listenerClient) { response ->
                    handleResponse(
                        response,
                        WaitingRoomMsg::class.java,
                        ListenerException("Waiting Room Listener Error")
                    )
                }
            } catch (e: Exception) {
                close(e)
            }
        }
    }

    override suspend fun waitingRoomSender(msg: WaitingRoomMsg) {
        val json = JacksonConv.classToJson(msg)
        val body = json.toRequestBody("text/plain".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, WAITING_ROOM_LISTENER),
            method = "POST",
            body = body
        )
        return request.send(httpClient) { response ->
            if (response.code != 200) throw ListenerException("Send Msg Error")
        }

    }

    override suspend fun postWaitingRoom(userId: Int, token: String): WaitingRoom {
        val json = JacksonConv.classToJson(UserIdAndRuleId(userId, RULE_ID))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, WAITING_ROOM),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        val support = request.send(httpClient) { response ->
            handleResponse(response, WaitingRoomSupport::class.java, ServerWaitingRoomException())
        }
        return WaitingRoom(support.userId, support.username, support.gameId, support.ruleId)
    }

    override suspend fun getWaitingRoom(userId: Int, token: String): WaitingRoom {
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, WAITING_ROOM),
            method = "GET",
            headerKey = listOf("Authorization", "userId"),
            headerValue = listOf(token, userId.toString())
        )
        val support = request.send(httpClient) { response ->
            handleResponse(response, WaitingRoomSupport::class.java, ServerWaitingRoomException())
        }
        return WaitingRoom(support.userId, support.username, support.gameId, support.ruleId)
    }

    override suspend fun exitWaitingRoom(userId: Int, token: String): Int {
        val json = JacksonConv.classToJson(UserId(userId))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, WAITING_ROOM_EXIT),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        return request.send(httpClient) { response ->
            handleResponse(response, Int::class.java, ServerWaitingRoomException())
        }
    }

    override suspend fun getComplexUserInfo(userId: Int, token: String): UserComplexInfoRes {
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, COMPLEXT_USER_INFO(userId)),
            method = "GET",
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        return request.send(httpClient) { response ->
            handleResponse(response, UserComplexInfoRes::class.java)
        }
    }

    override suspend fun getGameInfo(userId: Int, token: String, gameId: Int): ServerGame {
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, GAME_INFO(gameId)),
            method = "GET",
            headerKey = listOf("Authorization", "userId"),
            headerValue = listOf(token, userId.toString())
        )
        return request.send(httpClient) { response ->
            val support =
                handleResponse(response, ServerSupportGame::class.java, ServerGameException())
            support.toServerGame()
        }
    }

    override suspend fun setReady(userId: Int, token: String, gameId: Int): ServerGame {
        val json = JacksonConv.classToJson(
            UserId(userId)
        )
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, SET_READY(gameId)),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        val support = request.send(httpClient) { response ->
            handleResponse(response, ServerSupportGame::class.java)
        }
        return support.toServerGame()
    }

    override suspend fun putShipsInBoard(
        userId: Int,
        token: String,
        gameId: Int,
        putShip: PutOrRemoveShip
    ): List<PutResponse_GridCell> {
        val json = JacksonConv.classToJson(
            SupportUserPutOrRemoveShip(
                userId,
                putShip.shipType.name.toLowerCase(),
                putShip.position.toSupportPosition(),
                putShip.direction.name,
                putShip.action.name
            )
        )
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, GAME_BUILDING(gameId)),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        return request.send(httpClient) { response ->
            handleResponse(
                response,
                Array<PutResponse_GridCell>::class.java,
                ServerGameException()
            ).toList()
        }
    }

    override suspend fun putAllShips(token:String,putShip: PutShips): List<PutResponse_GridCell> {
        val json = JacksonConv.classToJson(putShip)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, GAME_BATTLE(putShip.gameId)),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token),
        )
        return request.send(httpClient) { response ->
            handleResponse(
                response,
                Array<PutResponse_GridCell>::class.java,
                ServerGameException()
            )
        }.toList()
    }

    override suspend fun shot(
        userId: Int,
        token: String,
        gameId: Int,
        position: Position
    ): ShotResult {
        val json = JacksonConv.classToJson(
            UserIdAndPosition(
                userId, position.toServerPosition()
            )
        )
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, GAME_BATTLE(gameId)),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token),
        )
        return request.send(httpClient) { response ->
            handleResponse(
                response,
                ShotResult::class.java,
                ServerGameException()
            )
        }
    }

    override suspend fun getMyGrid(userId: Int, token: String, gameId: Int): List<GridCell> {
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, GAME_BATTLE(gameId)),
            method = "GET",
            headerKey = listOf("Authorization", "userId"),
            headerValue = listOf(token, userId.toString()),
        )
        return request.send(httpClient) { response ->
            handleResponse(
                response,
                Array<GridCell>::class.java,
                ServerGameException()
            )
        }.toList()
    }

    override suspend fun exitGame(userId: Int, token: String, gameId: Int): ServerGame {
        val json = JacksonConv.classToJson(UserId(userId))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, EXIT_GAME(gameId)),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        return request.send(httpClient) { response ->
            val support =
                handleResponse(response, ServerSupportGame::class.java, ServerGameException())
            support.toServerGame()
        }
    }

    override suspend fun endGame(userId: Int, token: String, gameId: Int): ServerGame {
        val json = JacksonConv.classToJson(UserId(userId))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, END_GAME(gameId)),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token)
        )
        return request.send(httpClient) { response ->
            val support =
                handleResponse(response, ServerSupportGame::class.java, ServerGameException())
            support.toServerGame()
        }
    }

    private fun processStatusCode(statusCode: Int, body: String?, serverException: Throwable) {
        if (statusCode != 200 && statusCode != 201) {
            when (statusCode) {
                401 -> throw UnauthorizedException()
                else -> throw JacksonConv.jsonToClass(body!!, serverException::class.java)
            }
        }
    }

    private fun <T> handleResponse(
        response: Response,
        bodyClass: Class<T>,
        serverException: ServerException = ServerProblem()
    ): T {
        val contentType = response.body?.contentType()
        val body = response.body?.string()
        val status = response.code
        processStatusCode(status, body, serverException)
        return if (response.isSuccessful && contentType != null) {
            try {
                JacksonConv.jsonToClass(body!!, bodyClass)
            } catch (e: Exception) {
                throw JacksonException(response)
            }
        } else {
            throw JacksonException(response = response)
        }
    }

    private fun handleLoginResponse(response: Response): UserIdAndToken {
        val contentType = response.body?.contentType()
        val body = response.body?.string()
        val status = response.code
        processStatusCode(status, body, LoginException())
        return if (response.isSuccessful && contentType != null) {
            try {
                val token = response.headers["Authorization"]
                UserIdAndToken(body!!.toInt(), token!!)
            } catch (e: Exception) {
                throw JacksonException(response)
            }
        } else {
            throw JacksonException(response = response)
        }
    }

    private fun buildRequest(
        url: URL,
        mode: Service.Mode = Service.Mode.AUTO,
        method: String,
        body: RequestBody? = null,
        headerKey: List<String>? = null,
        headerValue: List<String>? = null
    ): Request {
        val prevR = preBuildRequest(url, mode, method, body)
        return if (headerKey != null && headerValue != null && headerKey.size == headerValue.size && headerKey.isNotEmpty()) {
            for (i in headerValue.indices) {
                prevR.addHeader(headerKey[i], headerValue[i])
            }
            prevR.build()
        } else prevR.build()
    }

    private fun preBuildRequest(
        url: URL,
        mode: Service.Mode,
        method: String,
        body: RequestBody? = null,
    ): Request.Builder =
        with(Request.Builder()) {
            when (mode) {
                Service.Mode.FORCE_REMOTE -> cacheControl(CacheControl.FORCE_NETWORK)
                Service.Mode.FORCE_LOCAL -> cacheControl(CacheControl.FORCE_CACHE)
                else -> this
            }
        }.url(url).method(method, body)

    override suspend fun timeover(userId: Int, token: String, gameId: Int): TimeoverRes {
        print("início service timeover")
        val json = JacksonConv.classToJson(InputUserIdGameId(userId, gameId))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = buildRequest(
            url = URL(PROTOCOL, HOST, PORT, "api/time"),
            method = "POST",
            body = body,
            headerKey = listOf("Authorization"),
            headerValue = listOf(token),
        )
        print("fim service timeover")
        return request.send(httpClient) { response ->
            handleResponse(response, TimeoverRes::class.java, ServerGameException())
        }
    }
}

