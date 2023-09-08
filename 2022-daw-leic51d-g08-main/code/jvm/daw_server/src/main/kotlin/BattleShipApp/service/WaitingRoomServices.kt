package BattleShipApp.service

import BattleShipApp.domain.*
import BattleShipApp.repository.*
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WaitingRoomServices(
    private val dataBase: Data,
    private val rep: Repositories,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private fun getAllWaitingRoomInfo(transaction: Transaction): List<WaitingRoom> =
        rep.waitingRoomRep.getAllWaitingRoom(transaction)

    fun getAllWaitingRoomInfoTrans(): List<WaitingRoom> {
        val trans = dataBase.createTransaction()
        return trans.executeTransaction<List<WaitingRoom>>(trans) {
            getAllWaitingRoomInfo(it)
        }
    }

    fun setWaitingRoomGameId(transaction: Transaction, inp: InputUserIdGameId): WaitingRoom {
        return rep.waitingRoomRep.updateWaitingRoomGameId(transaction, inp)

    }

    fun setWaitingRoomGameIdTrans(inp: InputUserIdGameId): WaitingRoom {
        val trans = dataBase.createTransaction()
        return trans.executeTransaction<WaitingRoom>(trans) {
            setWaitingRoomGameId(it, inp)
        }
    }

    fun setWaitingRoomIsGo(transaction: Transaction, inp: InputUpdateWaitingRoomIsGo): WaitingRoom {
        return rep.waitingRoomRep.updateIsGoWaitingRoom(transaction, inp)
    }

    fun setWaitingRoomIsGoTarns(inp: InputUpdateWaitingRoomIsGo): WaitingRoom {
        val trans = dataBase.createTransaction()
        return trans.executeTransaction<WaitingRoom>(trans) {
            setWaitingRoomIsGo(it, inp)
        }
    }

    fun exitWaitingRoom(transaction: Transaction, userId: Int) =
        rep.waitingRoomRep.deleteWaitingRoom(transaction, userId)

    fun getWaitingRoom(transaction: Transaction, userId: Int) =
        rep.waitingRoomRep.getWaitingRoomByUser(transaction, userId)
}