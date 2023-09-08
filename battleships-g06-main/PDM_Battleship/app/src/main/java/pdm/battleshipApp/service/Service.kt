package pdm.battleshipApp.service

import kotlinx.coroutines.flow.Flow
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.domain.*

interface Service {
    enum class Mode { FORCE_REMOTE, FORCE_LOCAL, AUTO }

    suspend fun fetchRanking(mode: Mode = Mode.AUTO): List<UserDetail>
    suspend fun fetchAuthors(): Authors
    suspend fun login(username: String, pass: String): UserIdAndToken?
    suspend fun fetchUserInfo(userId: Int, token: String): UserDetail
    suspend fun register(username: String, pass: String): User
    suspend fun postWaitingRoom(userId: Int, token: String): WaitingRoom
    suspend fun getWaitingRoom(userId: Int, token: String): WaitingRoom
    suspend fun exitWaitingRoom(userId: Int, token: String): Int
    fun waitingRoomListener(): Flow<WaitingRoomMsg>
    suspend fun waitingRoomSender(msg: WaitingRoomMsg)
    suspend fun getComplexUserInfo(userId: Int, token: String): UserComplexInfoRes
    suspend fun getGameInfo(userId: Int, token: String, gameId: Int): ServerGame
    suspend fun setReady(userId: Int, token: String, gameId: Int): ServerGame
    suspend fun putShipsInBoard(
        userId: Int,
        token: String,
        gameId: Int,
        putShip: PutOrRemoveShip
    ): List<PutResponse_GridCell>

    suspend fun putAllShips(token: String,putShip: PutShips): List<PutResponse_GridCell>

    suspend fun shot(userId: Int, token: String, gameId: Int, position: Position): ShotResult
    suspend fun getMyGrid(userId: Int, token: String, gameId: Int): List<GridCell>
    suspend fun exitGame(userId: Int, token: String, gameId: Int): ServerGame
    suspend fun endGame(userId: Int, token: String, gameId: Int): ServerGame
    suspend fun timeover(userId: Int, token: String, gameId: Int): TimeoverRes
}