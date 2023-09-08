package BattleShipApp.repository

import BattleShipApp.domain.InputGetShipType
import BattleShipApp.domain.InputShipType
import BattleShipApp.domain.ShipType
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB


class ShipTypeRepository {

    fun createShipType(transaction: Transaction, inputShipType: InputShipType): ShipType {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("insert into shiptypes values (?,?,?,?)")
            pst.setInt(1, inputShipType.ruleId)
            pst.setString(2, inputShipType.shipName)
            pst.setInt(3, inputShipType.squares)
            pst.setInt(4, inputShipType.fleetQuantity)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val pst2 = conn.prepareStatement("select * from shiptypes where ruleid = ? and shipname = ?")
                pst2.setInt(1, inputShipType.ruleId)
                pst2.setString(2, inputShipType.shipName)
                val rst2 = pst2.executeQuery()
                if (rst2.next()) {
                    return ShipType(
                        rst2.getInt(1),
                        rst2.getString(2),
                        rst2.getInt(3),
                        rst2.getInt(4)
                    )
                }
                throw NotFoundException("Not found ship type")
            }
            throw FailException("Fail create ship type")
        } catch (e: Exception) {
            throw e
        }
    }

    fun getShipTypeByRuleIdAndShipName(transaction: Transaction, inputShipType: InputGetShipType): ShipType {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from shiptypes where ruleid = ? and shipname = ?")
        pst.setInt(1, inputShipType.ruleId)
        pst.setString(2, inputShipType.shipName)
        val rst = pst.executeQuery()
        if (rst.next()) {
            return ShipType(
                rst.getInt(1),
                rst.getString(2),
                rst.getInt(3),
                rst.getInt(4)
            )
        }
        throw NotFoundException("Not found ship type")
    }

    fun getAllShipTypeByRuleId(transaction: Transaction, ruleId: Int): List<ShipType> {
        val conn = (transaction as TransactionDB).conn
        val list = mutableListOf<ShipType>()
        val pst = conn.prepareStatement("select * from shiptypes where ruleid = ?")
        pst.setInt(1, ruleId)
        val rst = pst.executeQuery()
        while (rst.next()) {
            list.add(
                ShipType(
                    rst.getInt(1),
                    rst.getString(2),
                    rst.getInt(3),
                    rst.getInt(4)
                )
            )
        }
        return list
    }

    fun checkIfGameIdExists(transaction: Transaction, ruleId: Int, shipName: String): Boolean {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from shiptypes where ruleid =? and shipname =?")
        pst.setInt(1, ruleId)
        pst.setString(2, shipName)
        val rst = pst.executeQuery()
        return rst.next()
    }
}