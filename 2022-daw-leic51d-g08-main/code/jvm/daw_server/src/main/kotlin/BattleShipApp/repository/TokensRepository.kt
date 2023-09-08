package BattleShipApp.repository

import BattleShipApp.domain.InputUserToken
import BattleShipApp.errors.FailException
import BattleShipApp.errors.NotFoundException
import BattleShipApp.transactions.Transaction
import BattleShipApp.transactions.TransactionDB
import java.util.UUID

class TokensRepository {

    fun createUserToken(transaction: Transaction, userToken: InputUserToken): Int{
        val conn = (transaction as TransactionDB).conn
        try {
            val pst = conn.prepareStatement("insert into tokens values (?,?::uuid)")
            pst.setInt(1, userToken.userId)
            pst.setString(2, userToken.token.toString())
            val rst = pst.executeUpdate()
            if (rst == 1){
                val pst2 = conn.prepareStatement("select * from tokens where userId =?")
                pst2.setInt(1, userToken.userId)
                val rst2 = pst2.executeQuery()
                if (rst2.next()){
                    return rst2.getInt(1)
                }
                throw NotFoundException("Not found user from tokens")
            }
            throw FailException("Fail create user token")
        }catch (e: Exception){
            throw e
        }
    }

    fun getUserIdByToken(transaction: Transaction, userToken: String): Int? {
        val conn = (transaction as TransactionDB).conn
        val pst = conn.prepareStatement("select * from tokens where tokenver = ?::uuid")
        pst.setString(1, userToken)
        val rst = pst.executeQuery()
        if (rst.next())
            return rst.getInt(1)
        return null
    }


}