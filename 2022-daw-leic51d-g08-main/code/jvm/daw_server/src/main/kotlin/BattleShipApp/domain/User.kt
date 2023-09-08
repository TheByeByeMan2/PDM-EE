package BattleShipApp.domain

data class User(
    val userId: Int,
    val username: String,
    val passVer: String,
    val userState: UserState,
)

data class UserComplexInfo(
    val userId: Int,
    val userState: UserState,
    val gameId: Int?,
    val ruleId:Int?
)

enum class UserState{
    FREE, WAITING, BATTLE
}


/**
 * Gathers user info to create one
 * @param userName the name of the user
 * @param pass the password of the user
 */
data class UsernameAndPass(val userName: String, val pass: String)

data class Register(val userName: String, val pass: String)

/**
 * Gathers user info from database
 * @param userId the user id
 * @param name the name of the user
 * @param score the score of the user
 * @param gamesPlayed the gamesPlayed of the user
 */
data class UserDetail(val userId: Int, val name: String, val score: Int, val gamesPlayed: Int)

data class UserAndToken(val userId: Int, val name: String, val token: String)

/**
 * Gathers list of the users
 * @param users the list of users info
 */
data class ListUsersDetail(val users: List<UserDetail>)