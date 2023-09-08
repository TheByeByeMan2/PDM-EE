package BattleShipApp.repository

import BattleShipApp.domain.GameGridFleet
import BattleShipApp.domain.InputGridFleet
import BattleShipApp.domain.InputUserShipName
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB
import java.sql.Connection
import java.sql.ResultSet

class GameGridFleetRepository {

    private fun createOneGridFleet(transaction: Transaction, info: InputGridFleet):GameGridFleet{
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("insert into gamegridfleet values (?,?,?)")
            pst.setInt(1, info.userId)
            pst.setString(2, info.shipName)
            pst.setInt(3, info.quantity)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from gamegridfleet where userid = ?")
                pst2.setInt(1, info.userId)
                val rst2 = pst2.executeQuery()
                if (rst2.next()){
                    return resGameGridFleet(rst2)
                }
                throw NotFoundException("Not Found Game Grid Fleet")
            }
            throw FailException("Fail create Game Grid Fleet ")
        }catch (e: Exception){
             throw e
        }
    }

    fun createMultipleGridFleet(transaction: Transaction, ships: List<InputGridFleet>): List<GameGridFleet> {
        try {
            val res = mutableListOf<GameGridFleet>()
            ships.forEach {
                res.add(createOneGridFleet(transaction, it))
            }
            return res
        }catch (e: Exception){
            throw e
        }
    }

    fun updateGridFleet(transaction: Transaction, inputGridFleet: InputGridFleet): GameGridFleet {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("update gamegridfleet set remainingquantity = ? where userid =? and shipname = ?")
            pst.setInt(1, inputGridFleet.quantity)
            pst.setInt(2, inputGridFleet.userId)
            pst.setString(3, inputGridFleet.shipName)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from gamegridfleet where userid =? and shipname = ?")
                pst2.setInt(1,inputGridFleet.userId)
                pst2.setString(2,inputGridFleet.shipName)
                val rst2 = pst2.executeQuery()
                if (rst2.next() && rst2.getInt("remainingquantity") == inputGridFleet.quantity){
                    return resGameGridFleet(rst2)
                }
                throw NotFoundException("Not found gameGridFleet")
            }
            throw FailException("Fail gameGridFleet update")
        }catch (e: Exception){
            throw e
        }
    }


    fun getGridFleetsById(transaction: Transaction, userId: Int): List<GameGridFleet>{
        val conn = (transaction as TransactionDB).conn
        val list = mutableListOf<GameGridFleet>()
        val pst = conn.prepareStatement("select * from gamegridfleet where userid =?")
        pst.setInt(1, userId)
        val rst = pst.executeQuery()
        while (rst.next()){
            list.add(resGameGridFleet(rst))
        }
        return list
    }

    fun getGridFleetByIdAndShipName(transaction: Transaction, userAndShip: InputUserShipName): GameGridFleet{
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from gamegridfleet where userid =? and shipname =?")
        pst.setInt(1, userAndShip.userId)
        pst.setString(2, userAndShip.shipName)
        val rst = pst.executeQuery()
        if (rst.next()){
            return resGameGridFleet(rst)
        }
        throw NotFoundException("Not found game grid fleet")
    }

    private fun deleteOneGridFleet(conn: Connection, userId: Int, ship: String): Boolean{
        try {
            val pst = conn.prepareStatement("delete from gamegridfleet where userid = ? and shipname =?")
            pst.setInt(1, userId)
            pst .setString(2, ship)
            val rst = pst.executeUpdate()
            if (rst == 1){
                val stm = conn.prepareStatement("select * from gamegridfleet where userid =?")
                stm.setInt(1, userId)
                val rst2 = stm.executeQuery()
                return !rst2.next()
            }
            throw FailException("Fail delete game grid fleet with id = $userId and ship $ship")
        }catch (e:Exception){
            throw e
        }

    }

    fun deleteGridFleet(transaction: Transaction, userId: Int, ships: List<String>): Boolean{
        val conn = (transaction as TransactionDB).conn
        try {
            var res = true
            ships.forEach {
                res = deleteOneGridFleet(conn, userId, it)
            }
            return res
        }catch (e:Exception){
            throw e
        }
    }


    private fun resGameGridFleet(rst: ResultSet) = GameGridFleet(
        rst.getInt("userId"),
        rst.getString("shipName"),
        rst.getInt("remainingQuantity")
    )
}