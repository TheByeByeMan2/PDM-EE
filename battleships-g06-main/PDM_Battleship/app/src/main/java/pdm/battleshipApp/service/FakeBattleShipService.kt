package pdm.battleshipApp.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.domain.*
import pdm.battleshipApp.exceptions.LoginException
import pdm.battleshipApp.exceptions.ServerUnknownException
import pdm.battleshipApp.exceptions.UnauthorizedException
import pdm.battleshipApp.exceptions.UserAlreadyExist

class FakeBattleShipService : Service {

    class FakeUser(val id: Int, val username: String, val pass: String)
    class FakeUserToken(val id: Int, val token: String)
    class FakeUserData(val id: Int, val score: Int, val gamePlayed: Int)

    val authors = Authors(listOf("Author1", "Author2"), 0.1)
    val users = mutableMapOf<Int,FakeUser>(
        Pair(1, FakeUser(1, "FakePlayer1", "FakePlayer1Pass")),
        Pair(2,FakeUser(2, "FakePlayer2", "FakePlayer2Pass"))
    )
    val userIdAndTokens = mutableMapOf<Int,FakeUserToken>(
        Pair(1, FakeUserToken(1, "fake-token-player-1")),
        Pair(2, FakeUserToken(2, "fake-token-player-2"))
    )
    val usersData = mutableMapOf<Int,FakeUserData>(
        Pair(1, FakeUserData(1, 100, 90)),
        Pair(2, FakeUserData(2, 90, 80))
    )

    override suspend fun fetchRanking(mode: Service.Mode): List<UserDetail> {
        delay(2000)
        val list = mutableListOf<UserDetail>()
        usersData.forEach {
            val userData = it.value
            val userName = users[it.key]?.username
            if (userName != null){
                list.add(UserDetail(userData.id, userName, userData.score, userData.gamePlayed))
            } else {
                throw ServerUnknownException("Ranking Panic exception")
            }
        }
        return list
    }

    fun fakeAuthentication(userId: Int, token: String) {
        val userToken = userIdAndTokens[userId]
        if (userToken != null){
            if (userToken.token != token){
                throw ServerUnknownException("Authentication Panic Exception")
            }
        } else {
            throw UnauthorizedException()
        }
    }

    override suspend fun fetchAuthors(): Authors {
        delay(1000)
        return authors
    }

    override suspend fun login(username: String, pass: String): UserIdAndToken {
        delay(3000)
        var flag = false
        var userId = 0
        users.forEach { (t, u) ->
            if (u.username == username && u.pass == pass){
                flag = true
                userId = t
            }
        }
        if (!flag || userId == 0) throw LoginException("Non user")
        val token = userIdAndTokens[userId]?.token
        if (token == null){
            throw ServerUnknownException("Login Panic Exception")
        } else {
            return UserIdAndToken(userId, token)
        }
    }

    override suspend fun fetchUserInfo(userId: Int, token: String): UserDetail {
        fakeAuthentication(userId, token)
        val username = users.get(userId)?.username
        val userData = usersData.get(userId)
        if (userData != null && username != null)
            return UserDetail(userId, username, userData.score, userData.gamePlayed)
        else throw ServerUnknownException("Fetch User Info Panic Exception")
    }

    override suspend fun register(username: String, pass: String): pdm.battleshipApp.domain.User {
        var flag = false
        users.forEach { (t, u) ->
            if (u.username == username) flag = true
        }
        if (flag) throw UserAlreadyExist()
        val userId = users.keys.size +1
        users.put(userId, FakeUser(userId, username, pass))
        usersData.put(userId, FakeUserData(userId, 0, 0))
        userIdAndTokens.put(userId, FakeUserToken(userId, "fake-player-token"))
        return pdm.battleshipApp.domain.User(userId, username, UserState.FREE)
    }

    override suspend fun postWaitingRoom(userId: Int, token: String): WaitingRoom {
        TODO("Not yet implemented")
    }

    override suspend fun getWaitingRoom(userId: Int, token: String): WaitingRoom {
        TODO("Not yet implemented")
    }

    override suspend fun exitWaitingRoom(userId: Int, token: String): Int {
        TODO("Not yet implemented")
    }

    override fun waitingRoomListener(): Flow<WaitingRoomMsg> {
        TODO("Not yet implemented")
    }

    override suspend fun waitingRoomSender(msg: WaitingRoomMsg) {
        TODO("Not yet implemented")
    }

    override suspend fun getComplexUserInfo(userId: Int, token: String): UserComplexInfoRes {
        TODO("Not yet implemented")
    }

    override suspend fun getGameInfo(userId: Int, token: String, gameId: Int): ServerGame {
        TODO("Not yet implemented")
    }

    override suspend fun setReady(userId: Int, token: String, gameId: Int): ServerGame {
        TODO("Not yet implemented")
    }

    override suspend fun putShipsInBoard(
        userId: Int,
        token: String,
        gameId: Int,
        putShip: PutOrRemoveShip
    ): List<PutResponse_GridCell> {
        TODO("Not yet implemented")
    }

    override suspend fun putAllShips(token: String,putShip: PutShips): List<PutResponse_GridCell> {
        TODO("Not yet implemented")
    }

    override suspend fun shot(
        userId: Int,
        token: String,
        gameId: Int,
        position: Position
    ): ShotResult {
        TODO("Not yet implemented")
    }

    override suspend fun getMyGrid(userId: Int, token: String, gameId: Int): List<GridCell> {
        TODO("Not yet implemented")
    }

    override suspend fun exitGame(userId: Int, token: String, gameId: Int): ServerGame {
        TODO("Not yet implemented")
    }

    override suspend fun endGame(userId: Int, token: String, gameId: Int): ServerGame {
        TODO("Not yet implemented")
    }

    override suspend fun timeover(userId: Int, token: String, gameId: Int): TimeoverRes {
        TODO("Not yet implemented")
    }


}