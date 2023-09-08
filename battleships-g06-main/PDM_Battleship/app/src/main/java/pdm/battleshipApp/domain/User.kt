package pdm.battleshipApp.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pdm.battleshipApp.battleShip.model.coords.Position

enum class UserState{
    FREE, WAITING, BATTLE
}

data class UserSupport(
    val userId: Int,
    val username: String,
    val passVer: String,
    val userState: UserState
)
data class User(val userId: Int, val username: String, val userState: UserState)

data class UserDetail(val userId: Int, val name: String, val score: Int, val gamesPlayed: Int)

data class UsernameAndPass(val userName: String, val pass: String)

@Parcelize
data class UserIdAndToken(val userId: Int, val token: String) : Parcelable

@Parcelize
data class LocalData(val userId: Int, val token: String, val username: String) : Parcelable

data class UserIdAndRuleId(val userId: Int, val ruleId: Int)

data class UserId(val userId: Int)

data class UserComplexInfoRes(val userId: Int, val userState: UserState, val gameId: Int?, val ruleId: Int?)

data class UserIdAndPosition(val userId: Int, val position: ServerPosition)