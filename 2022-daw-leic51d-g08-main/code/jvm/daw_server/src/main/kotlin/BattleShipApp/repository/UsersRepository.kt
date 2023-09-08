package BattleShipApp.repository

import BattleShipApp.domain.*
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.errors.UnauthenticatedException
import BattleShipApp.utils.toUserState
import java.sql.Connection

class UsersRepository() {

    /**
     *
     */
    fun getUserIdByNameAndPass(
        transaction: Transaction,
        user: UsernameAndPass
    ): Int {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from users where name = ? and passVer = ?")
        pst.setString(1, user.userName)
        pst.setString(2, user.pass)
        val rst = pst.executeQuery()
        if (rst.next()) {
            return rst.getInt(1)
        }
        throw UnauthenticatedException("The username or the password were incorrect")
    }

    fun getUserAndTokenById(transaction: Transaction, userId: Int): UserAndToken{
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select u.id, u.name, t.tokenver from users u join tokens t on u.id = t.userid where u.id =?")
        pst.setInt(1, userId)
        val rst = pst.executeQuery()
        if (rst.next()){
            return UserAndToken(rst.getInt(1), rst.getString(2), rst.getString(3))
        }
        throw NotFoundException("Not found user")
    }

    fun getUserById(transaction: Transaction, id: Int): User {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from users where id = ?")
        pst.setInt(1, id)
        val rst = pst.executeQuery()
        if (rst.next()) {
            return User(rst.getInt(1), rst.getString(2), rst.getString(3), rst.getString(4).toUserState())
        } else throw UnauthenticatedException("The id is incorrect")
    }

    fun getUserByName(transaction: Transaction, name: String): User {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from users where name = ?")
        pst.setString(1, name)
        val rst = pst.executeQuery()
        if (rst.next()) {
            return User(rst.getInt(1), rst.getString(2), rst.getString(3), rst.getString(4).toUserState())
        } else throw UnauthenticatedException("The name is incorrect")
    }

    /**
     * Check if the user id was already created
     * @param transaction transaction needed to execute the action safely
     * @param userId the user id to be found
     * @return the user true if exists or false if not
     */
    fun checkIfUserIdExists(transaction: Transaction, userId: Int): Boolean {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from users where id = ?")
        pst.setInt(1, userId)
        val rst = pst.executeQuery()
        return rst.next()
    }

    fun checkIfUserNameExists(transaction: Transaction, username: String): Boolean {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from users where name = ?")
        pst.setString(1, username)
        val rst = pst.executeQuery()
        return rst.next()
    }

    /**
     * Create a user, verifying if the email is already in used and create a new user
     * @param transaction transaction needed to execute the action safely
     * @param userData the user information that will be stored
     * @return [Pair<Int, String>] Returns the new id and username
     */
    fun createUser(transaction: Transaction, userData: UsernameAndPass): User {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("insert into users(name,passVer,userstate) values(?,?,?)")
            pst.setString(1, userData.userName)
            pst.setString(2, userData.pass)
            pst.setString(3, UserState.FREE.name)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val stm = conn.prepareStatement("select * from users order by id desc limit 1")
                val rst2 = stm.executeQuery()
                if (rst2.next()) {
                    val rs = createUserData(conn, userId = rst2.getInt("id"))
                    if (rs) {
                        return User(rst2.getInt(1), rst2.getString(2), rst2.getString(3), rst2.getString(4).toUserState())
                    } else throw FailException("Error creating user_data")
                }
            }
            throw FailException("Error creating user")
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     *
     *
     */
    private fun createUserData(conn: Connection, userId: Int): Boolean {
        try {
            val pst = conn.prepareStatement("insert into user_data(userId,score, gamesPlayed) values(?,?,?)")
            pst.setInt(1, userId)
            pst.setInt(2, 0)
            pst.setInt(3, 0)
            val rst = pst.executeUpdate()
            return rst == 1
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Get the detail of a certain user
     * @param transaction transaction needed to execute the action safely
     * @param userId  identifier of the user
     * @return [UserDetail] the details of the user
     */
    fun getUserDetailsById(transaction: Transaction, userId: Int): UserDetail {
        val conn = (transaction as TransactionDB).conn
        val pst =
            conn.prepareStatement("select id, name, score, gamesPlayed from users u join user_data ud on u.id=ud.userId where u.id = ? ")
        pst.setInt(1, userId)
        val rst = pst.executeQuery()
        if (rst.next()) {
            return UserDetail(
                userId = rst.getInt(1),
                name = rst.getString(2),
                score = rst.getInt(3),
                gamesPlayed = rst.getInt(4)
            )
        }
        throw FailException("Error getting user details")
    }


    /**
     * Give all the users, that were created
     * @param transaction transaction needed to execute the action safely
     * @param limit the maximum number of users
     * @return List<OutputUser> the list of users
     */
    fun usersDetailsList(transaction: Transaction, limit: Int): List<UserDetail> {
        val conn = (transaction as TransactionDB).conn
        val usersList = mutableListOf<UserDetail>()
        val pst =
            conn.prepareStatement("select * from users u join user_data ud on u.id = ud.userId order by ud.score desc limit ? ")
        pst.setInt(1, limit)
        val rst = pst.executeQuery()
        while (rst.next()) {
            usersList.add(
                UserDetail(
                    userId = rst.getInt("userId"),
                    name = rst.getString("name"),
                    score = rst.getInt("score"),
                    gamesPlayed = rst.getInt("gamesPlayed")
                )
            )
        }
        return usersList
    }

    /**
     *
     *
     */
    fun getUsersSize(transaction: Transaction): Int? {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select count(userid) from user_data")
        val rst = pst.executeQuery()
        return if (rst.next()) {
            rst.getInt(1)
        } else null
    }

    /**
     *
     * @param uus the users ids and how much want inc or dec user score
     */
    fun updateUsersScoreAndGamePlayed(transaction: Transaction, uus: InputUsersAndScores): List<UserDetail> {
        val conn = (transaction as TransactionDB).conn
        try {
            return mutableListOf(
                updateUserScoreAndGamePlayed(conn, InputUserAndScore(uus.userIdA, uus.tScoreA)),
                updateUserScoreAndGamePlayed(conn, InputUserAndScore(uus.userIdB, uus.tScoreB))
            )
        } catch (e: Exception) {
            throw e
        }
    }

    private fun updateUserScoreAndGamePlayed(conn: Connection, inputUserAndScore: InputUserAndScore): UserDetail {
        try {
            val pst = conn.prepareStatement("select * from user_data where userid =?")
            pst.setInt(1, inputUserAndScore.userId)
            val rst = pst.executeQuery()
            if (rst.next()) {
                val id = rst.getInt(1)
                val score = rst.getInt(2)
                val gamePlayed = rst.getInt(3)
                val invGamesPlayed = gamePlayed + 1
                val sumScore = score + inputUserAndScore.score
                val nextScore = if (sumScore < 0) 0
                else sumScore
                val pst2 = conn.prepareStatement("update user_data set score =?, gamesplayed =? where userid =?")
                pst2.setInt(1, nextScore)
                pst2.setInt(2, invGamesPlayed)
                pst2.setInt(3, inputUserAndScore.userId)
                val rst2 = pst2.executeUpdate()
                if (rst2 == 1) {
                    val pst3 = conn.prepareStatement("select id, name, score, gamesPlayed from users u join user_data ud on u.id=ud.userId where u.id = ? ")
                    pst3.setInt(1, inputUserAndScore.userId)
                    val rst3 = pst3.executeQuery()
                    if (rst3.next()) {
                        val currentScore = rst3.getInt(3)
                        val currentGamesPlayed = rst3.getInt(4)
                        if (currentScore == nextScore && currentGamesPlayed == invGamesPlayed) {
                            return UserDetail(
                                userId = rst3.getInt(1),
                                name = rst3.getString(2),
                                score = rst3.getInt(3),
                                gamesPlayed = rst3.getInt(4)
                            )
                        }
                    }
                    throw NotFoundException("Not found user 1")
                }
                throw FailException("Fail execute update")
            }
            throw NotFoundException("Not found user 2")
        } catch (e: Exception) {
            throw e
        }
    }

    fun updateUserState(transaction: Transaction, userIdAndUserState: InputUserIdAndUserState): User{
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("update users set userstate =? where id =?")
            pst.setString(1,userIdAndUserState.userState.name)
            pst.setInt(2, userIdAndUserState.userId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from users where id = ?")
                pst2.setInt(1,userIdAndUserState.userId)
                val rst2 = pst2.executeQuery()
                if (rst2.next()){
                    return User(rst2.getInt(1), rst2.getString(2), rst2.getString(3), rst2.getString(4).toUserState())
                }
                throw NotFoundException("Not found user when update user state")
            }
            throw FailException("Fail execute update user state")
        }catch (e: Exception){
            throw e
        }
    }

    /**
     * Change the password from userId
     * @param transaction transaction needed to execute the action safely
     * @param userId the user identifier
     * @param oldPassword the old password user value
     * @param newPassword the new password user value
     * @return Boolean the result
     */
    fun changeUserPassword(
        transaction: Transaction,
        userId: Int,
        oldPassword: String,
        newPassword: String
    ): Boolean {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("update users set passVer = ? where id = ? and passVer = ?")
        pst.setString(1, newPassword)
        pst.setInt(2, userId)
        pst.setString(3, oldPassword)
        val rst = pst.executeUpdate()
        return if (rst == 1) true else throw UnauthenticatedException("The user or the password are wrong")
    }
}
