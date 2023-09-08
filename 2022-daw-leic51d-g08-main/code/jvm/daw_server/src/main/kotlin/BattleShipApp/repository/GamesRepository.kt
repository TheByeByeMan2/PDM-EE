package BattleShipApp.repository

import BattleShipApp.domain.*
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.utils.GameState
import BattleShipApp.utils.toGameState
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp

class GamesRepository() {

    enum class UserType { A, B }
    enum class Finish { First, Second }

    /**
     *
     *
     */
    fun createGame(transaction: Transaction, usersAndRule: InputCreateGame): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst =
                conn.prepareStatement("insert into games(userA,userB,readya, readyb, finisha, finishb, initialturn, turn, ruleid, remainingshot, gamestate) values(?,?,?,?,?,?,?,?,?,?,?)")
            pst.setInt(1, usersAndRule.userIdA)
            pst.setInt(2, usersAndRule.userIdB)
            pst.setBoolean(3, false)
            pst.setBoolean(4, false)
            pst.setBoolean(5, false)
            pst.setBoolean(6, false)
            pst.setTimestamp(7, null)
            pst.setInt(8, usersAndRule.userIdA)
            pst.setInt(9, usersAndRule.ruleId)
            pst.setInt(10, usersAndRule.remainingShot)
            pst.setString(11, GameState.START.name)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val stm = conn.prepareStatement("select * from games where userA = ? and userB = ? ")
                stm.setInt(1, usersAndRule.userIdA)
                stm.setInt(2, usersAndRule.userIdB)
                val rst2 = stm.executeQuery()
                if (rst2.next()) {
                    return resGame(rst2)
                }
            }
            throw FailException("Error creating game")
        } catch (e: Exception) {
            throw e
        }
    }


    /**
     *
     *
     */
    fun updateInitialTurn(transaction: Transaction, gameId: Int): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("UPDATE games set initialTurn = ? where gameId = ?")
            val time = Timestamp(System.currentTimeMillis())
            pst.setTimestamp(1, time)
            pst.setInt(2, gameId)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val stm = conn.prepareStatement("select * from games where gameId = ? ")
                stm.setInt(1, gameId)
                val rst2 = stm.executeQuery()
                if (rst2.next() && rst2.getTimestamp("initialTurn").equals(time)) {
                    return resGame(rst2)
                }
            }
            throw NotFoundException("Game not found")
        } catch (e: Exception) {
            throw e
        }
    }


    /**
     *
     *
     */
    fun getGameIdFromUsers(transaction: Transaction, users: InputGameUsersAndRuleId): Int {
        val conn = (transaction as TransactionDB).conn
        try {
            val stm = conn.prepareStatement("select gameId from games where userA = ? and userB = ? ")
            stm.setInt(1, users.userIdA)
            stm.setInt(2, users.userIdB)
            val rst = stm.executeQuery()
            if (rst.next()) {
                return rst.getInt("gameId")
            }
            throw NotFoundException("game not found")
        } catch (e: Exception) {
            throw e
        }
    }

    fun getGameById(transaction: Transaction, gameId: Int): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val stm = conn.prepareStatement("select * from games where gameId = ? ")
            stm.setInt(1, gameId)
            val rst = stm.executeQuery()
            if (rst.next()) {
                return resGame(rst)
            }
            throw NotFoundException("game not found")
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     *
     *
     *
     */
    fun setGameWinner(transaction: Transaction, gameWinnerInput: InputGameWinner): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("UPDATE games set winner = ? where gameId = ?")
            pst.setString(1, gameWinnerInput.winner)
            pst.setInt(2, gameWinnerInput.gameId)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val stm = conn.prepareStatement("select * from games where gameId = ? ")
                stm.setInt(1, gameWinnerInput.gameId)
                val rst2 = stm.executeQuery()
                if (rst2.next()) {
                    return resGame(rst2)
                }
            }
            throw NotFoundException("game not found")
        } catch (e: Exception) {
            throw e
        }
    }
    private fun getUserType(conn: Connection, gameIdAndUserId: InputUserIdGameId): UserType {
        val pst = conn.prepareStatement("select usera, userb from games where gameid = ?")
        pst.setInt(1, gameIdAndUserId.gameId)
        val rst = pst.executeQuery()
        if (rst.next()) {
            val userA = rst.getInt(1)
            val userB = rst.getInt(2)
            if (userA == gameIdAndUserId.userId) return UserType.A
            if (userB == gameIdAndUserId.userId) return UserType.B
            throw FailException("Fail get users")
        }
        throw NotFoundException("game id not found")
    }


    fun setGameReady(transaction: Transaction, gameIdAndUserId: InputUserIdGameId): Game{
        val conn = (transaction as TransactionDB).conn
        try {
            val userType = getUserType(conn, gameIdAndUserId)
            val sql = if (userType === UserType.A) "UPDATE games set readyA = ? where gameId = ?"
            else "UPDATE games set readyB = ? where gameId = ?"
            val pst = conn.prepareStatement(sql)
            pst.setBoolean(1, true)
            pst.setInt(2, gameIdAndUserId.gameId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val sql2 = if (userType === UserType.A) "select readya from games where gameid =?"
                else "select readyb from games where gameid =?"
                val pst2 = conn.prepareStatement(sql2)
                pst2.setInt(1,gameIdAndUserId.gameId)
                val rst2 = pst2.executeQuery()
                if (rst2.next() && rst2.getBoolean(1)){
                    val pst3 = conn.prepareStatement("select * from games where gameid=?;")
                    pst3.setInt(1,gameIdAndUserId.gameId)
                    val rst3 = pst3.executeQuery()
                    if (rst3.next()){
                        return resGame(rst3)
                    }
                    throw NotFoundException("Not found the game")
                }
                throw FailException("Fail update game ready")
            }
            throw FailException("Fail update game ready")
        }catch (e: Exception){
            throw e
        }

    }

    fun setGameFinish(transaction: Transaction, gameIdAndUserId: InputUserIdGameId): Game{
        val conn = (transaction as TransactionDB).conn
        try {
            val userType = getUserType(conn, gameIdAndUserId)
            val sql = if (userType === UserType.A) "UPDATE games set finishA = ? where gameId = ?"
            else "UPDATE games set finishB = ? where gameId = ?"
            val pst = conn.prepareStatement(sql)
            pst.setBoolean(1, true)
            pst.setInt(2, gameIdAndUserId.gameId)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val sql2 = if (userType === UserType.A) "select finishA from games where gameid =?"
                else "select finishB from games where gameid =?"
                val stm = conn.prepareStatement(sql2)
                stm.setInt(1, gameIdAndUserId.gameId)
                val rst2 = stm.executeQuery()
                if (rst2.next() && rst2.getBoolean(1)) {
                    val pst3 = conn.prepareStatement("select * from games where gameid=?;")
                    pst3.setInt(1,gameIdAndUserId.gameId)
                    val rst3 = pst3.executeQuery()
                    if (rst3.next()){
                        return resGame(rst3)
                    }
                    throw NotFoundException("Not found the game")
                }
            }
            throw FailException("Fail execute game finish update")
        } catch (e: Exception) {
            throw e
        }
    }


    /**
     * Pass to other turn automatic
     *
     */
    fun switchTurn(transaction: Transaction, gameId: Int): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("select turn, usera, userb from games where gameid = ?")
            pst.setInt(1, gameId)
            val rst = pst.executeQuery()
            if (rst.next()) {
                val turn = rst.getInt(1)
                val userA = rst.getInt(2)
                val userB = rst.getInt(3)
                val nextTurn = if (turn == userA) {
                    userB
                } else userA
                val pst2 = conn.prepareStatement("UPDATE games set turn = ? where gameId = ?")
                pst2.setInt(1, nextTurn)
                pst2.setInt(2, gameId)
                val rst2 = pst2.executeUpdate()
                if (rst2 == 1) {
                    val stm = conn.prepareStatement("select * from games where gameId = ?")
                    stm.setInt(1, gameId)
                    val rst3 = stm.executeQuery()
                    if (rst3.next() && rst3.getInt("turn") == nextTurn) {
                        return resGame(rst3)
                    }
                }
            }
            throw FailException("Fail execute update")
        } catch (e: Exception) {
            throw e
        }
    }

    fun updatePerShot(transaction: Transaction, gameIdAndNShot: InputUpdatePerShot): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("update games set remainingshot = ? where gameid = ?")
            pst.setInt(1,gameIdAndNShot.remainingShot)
            pst.setInt(2, gameIdAndNShot.gameId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val smt = conn.prepareStatement("select * from games where gameid =?")
                smt.setInt(1, gameIdAndNShot.gameId)
                val rst2 = smt.executeQuery()
                if (rst2.next()){
                    return resGame(rst2)
                }
                throw NotFoundException("Not found game")
            }
            throw FailException("Fail update game")
        }catch (e: Exception){
            throw e
        }
    }

    fun updateGameState(transaction: Transaction, gameIdAndGameState: InputUpdateGameState): Game {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("update games set gamestate = ? where gameid = ?")
            pst.setString(1,gameIdAndGameState.gameState.name)
            pst.setInt(2, gameIdAndGameState.gameId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val smt = conn.prepareStatement("select * from games where gameid =?")
                smt.setInt(1, gameIdAndGameState.gameId)
                val rst2 = smt.executeQuery()
                if (rst2.next()){
                    return resGame(rst2)
                }
                throw NotFoundException("Not found game")
            }
            throw FailException("Fail update game")
        }catch (e: Exception){
            throw e
        }
    }

    fun deleteGame(transaction: Transaction, gameId: Int): Boolean{
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("delete from games where gameId = ?")
            pst.setInt(1,gameId)
            val rst = pst.executeUpdate()
            return rst == 1
        } catch (e: Exception){
            throw e
        }
    }

    fun getGameByUserId(transaction: Transaction, userId:Int):Game?{
        val conn = (transaction as TransactionDB).conn
        try {
            val stm = conn.prepareStatement("select * from games where userA = ? or userB = ? ")
            stm.setInt(1, userId)
            stm.setInt(2, userId)
            val rst = stm.executeQuery()
            if (rst.next()) {
                return resGame(rst)
            } else return null
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Check if the game id was already created
     * @param transaction transaction needed to execute the action safely
     * @param gameId the gameId id to be found
     * @return the return true if exists or false if not
     */
    fun checkIfGameIdExists(transaction: Transaction, gameId: Int): Boolean {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from games where gameId = ?")
        pst.setInt(1, gameId)
        val rst = pst.executeQuery()
        return rst.next()
    }


    private fun resGame(rst: ResultSet) = Game(
        rst.getInt("gameId"),
        rst.getInt("userA"),
        rst.getInt("userB"),
        rst.getInt("turn"),
        rst.getTimestamp("initialTurn"),
        rst.getBoolean("readya"),
        rst.getBoolean("readyb"),
        rst.getString("winner"),
        rst.getBoolean("finisha"),
        rst.getBoolean("finishb"),
        rst.getInt("ruleId"),
        rst.getInt("remainingShot"),
        rst.getString("gamestate").toGameState()
    )

}