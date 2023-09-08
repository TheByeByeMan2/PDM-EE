package BattleShipApp.repository

import BattleShipApp.domain.InputUserIdGameId
import BattleShipApp.domain.InputUpdateWaitingRoomIsGo
import BattleShipApp.domain.JoinWaitingRoomInfo
import BattleShipApp.domain.WaitingRoom
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB
import java.sql.Timestamp

class WaitingRoomRepository {

    private fun Int.nullableInt() = if (this == 0) null else this

    fun getAllWaitingRoom(transaction: Transaction): List<WaitingRoom> {
        val conn = (transaction as TransactionDB).conn
        val l = mutableListOf<WaitingRoom>()
        val pst = conn.prepareStatement("select * from waitingroom order by time")
        val rst = pst.executeQuery()
        while (rst.next()) {
            val waitingRoom = WaitingRoom(
                rst.getInt(1),
                rst.getString(2),
                rst.getInt(3).nullableInt(),
                rst.getBoolean(4),
                rst.getTimestamp(5),
                rst.getInt(6)
            )
            l.add(waitingRoom)
        }
        return l
    }

    fun getWaitingRoomByUser(transaction: Transaction, userId: Int):WaitingRoom{
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from waitingroom where userid = ?")
        pst.setInt(1, userId)
        val rst = pst.executeQuery()
        if (rst.next()){
            return WaitingRoom(
                rst.getInt(1),
                rst.getString(2),
                rst.getInt(3).nullableInt(),
                rst.getBoolean(4),
                rst.getTimestamp(5),
                rst.getInt(6)
            )
        } else throw NotFoundException("Waiting Room not found")
    }

    fun getWaitingRoomOrNullByUser(transaction: Transaction, userId: Int):WaitingRoom?{
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from waitingroom where userid = ?")
        pst.setInt(1, userId)
        val rst = pst.executeQuery()
        if (rst.next()){
            return WaitingRoom(
                rst.getInt(1),
                rst.getString(2),
                rst.getInt(3).nullableInt(),
                rst.getBoolean(4),
                rst.getTimestamp(5),
                rst.getInt(6)
            )
        } else return null
    }

    fun createWaitingRoom(transaction: Transaction, info: JoinWaitingRoomInfo): WaitingRoom {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("select name from users where id = ?")
            pst.setInt(1, info.userId)
            val rst = pst.executeQuery()
            if (rst.next()) {
                val username = rst.getString(1)

                val pst2 = conn.prepareStatement("insert into waitingroom(userid, username, isgo, time, ruleid) values (?,?,?,?, ?)")
                pst2.setInt(1, info.userId)
                pst2.setString(2, username)
                pst2.setBoolean(3, false)
                val time = Timestamp(System.currentTimeMillis())
                pst2.setTimestamp(4, time)
                pst2.setInt(5, info.ruleId)
                val rst2 = pst2.executeUpdate()
                if (rst2 == 1) {
                    val pst3 = conn.prepareStatement("select * from waitingroom where userid = ?")
                    pst3.setInt(1, info.userId)
                    val rst3 = pst3.executeQuery()
                    if (rst3.next() && rst3.getInt(1) == info.userId) {

                        return WaitingRoom(
                            rst3.getInt(1),
                            rst3.getString(2),
                            rst3.getInt(3).nullableInt(),
                            rst3.getBoolean(4),
                            rst3.getTimestamp(5),
                            rst3.getInt(6)
                        )
                    } else throw NotFoundException("Not found waitingRoom with user id = ${info.userId}")

                } else throw FailException("Error insert into waitingRoom")

            } else throw NotFoundException("Not found user with user id =  ${info.userId}")
        } catch (e: Exception) {
            throw e
        }
    }

    fun updateWaitingRoomGameId(transaction: Transaction, inputUserIdGameId: InputUserIdGameId): WaitingRoom {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("update waitingroom set gameid = ? where userid = ?")
            pst.setInt(1, inputUserIdGameId.gameId)
            pst.setInt(2, inputUserIdGameId.userId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from waitingroom where userid = ?;")
                pst2.setInt(1, inputUserIdGameId.userId)
                val rst2 = pst2.executeQuery()
                if (rst2.next()){
                    return WaitingRoom(
                        rst2.getInt(1),
                        rst2.getString(2),
                        rst2.getInt(3).nullableInt(),
                        rst2.getBoolean(4),
                        rst2.getTimestamp(5),
                        rst2.getInt(6)
                    )
                }else throw NotFoundException("The waiting room is not found")
            }else throw FailException("Update WaitingRoom fail")
        }catch (e: Exception) {
            throw e
        }
    }

    fun updateIsGoWaitingRoom(transaction: Transaction, inputUpdateWaitingRoomIsGo: InputUpdateWaitingRoomIsGo): WaitingRoom {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("update waitingroom set isgo = ? where userid = ?")
            pst.setBoolean(1, inputUpdateWaitingRoomIsGo.isGo)
            pst.setInt(2, inputUpdateWaitingRoomIsGo.userId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from waitingroom where userid = ?;")
                pst2.setInt(1, inputUpdateWaitingRoomIsGo.userId)
                val rst2 = pst2.executeQuery()
                if (rst2.next()){
                    return WaitingRoom(
                        rst2.getInt(1),
                        rst2.getString(2),
                        rst2.getInt(3).nullableInt(),
                        rst2.getBoolean(4),
                        rst2.getTimestamp(5),
                        rst2.getInt(6)
                    )
                }else throw NotFoundException("The waiting room is not found")
            }else throw FailException("Update WaitingRoom fail")
        }catch (e: Exception) {
            throw e
        }
    }

    fun deleteWaitingRoom(transaction: Transaction,userId: Int): Int{
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("delete from waitingroom where userid =?")
            pst.setInt(1,userId)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from waitingroom where userid = ?")
                pst2.setInt(1, userId)
                val rst2 = pst2.executeQuery()
                if (!rst2.next()){
                    return userId
                } else throw FailException("Found room after delete")
            } else throw FailException("fail delete waitingRoom")
        }catch (e: Exception){
            throw e
        }
    }

}