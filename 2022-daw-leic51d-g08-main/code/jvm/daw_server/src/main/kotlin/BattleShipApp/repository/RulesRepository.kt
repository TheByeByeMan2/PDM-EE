package BattleShipApp.repository

import BattleShipApp.domain.InputCreateRule
import BattleShipApp.domain.Rule
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB


class RulesRepository {

    fun createRule(transaction: Transaction, inputRule: InputCreateRule): Rule {
        val conn = (transaction as TransactionDB).conn
        try {
            val pst =
                conn.prepareStatement("insert into rules(grid_size, Number_of_shots, player_timeout) values (?,?,?)")
            pst.setString(1, inputRule.gridSize)
            pst.setInt(2, inputRule.shotNumber)
            pst.setInt(3, inputRule.timeout)
            val rst = pst.executeUpdate()
            if (rst == 1) {
                val pst2 = conn.prepareStatement("select * from rules order by ruleid desc limit 1;")
                val rst2 = pst2.executeQuery()
                if (rst2.next()) {
                    return Rule(
                        rst2.getInt(1),
                        rst2.getString(2),
                        rst2.getInt(3),
                        rst2.getInt(4)
                    )
                }
                throw NotFoundException("Not found rule created")
            }
            throw FailException("Error creating rule")
        } catch (e: Exception) {
            throw e
        }
    }

    fun getRule(transaction: Transaction, ruleId: Int): Rule {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from rules where ruleid = ?")
        pst.setInt(1, ruleId)
        val rst = pst.executeQuery()
        if (rst.next()) {
            return Rule(
                rst.getInt(1),
                rst.getString(2),
                rst.getInt(3),
                rst.getInt(4)
            )
        }
        throw NotFoundException("Not found rule")
    }

    fun getAllRules(transaction: Transaction): List<Rule>{
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from rules")
        val rst = pst.executeQuery()
        val res = mutableListOf<Rule>()
        while (rst.next()) {
           res.add(
               Rule(
                   rst.getInt(1),
                   rst.getString(2),
                   rst.getInt(3),
                   rst.getInt(4),
               )
           )
        }
        return res
    }

    fun checkIfRuleIdExists(transaction: Transaction, ruleId: Int): Boolean {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from rules where ruleid = ?")
        pst.setInt(1, ruleId)
        val rst = pst.executeQuery()
        return rst.next()
    }

}