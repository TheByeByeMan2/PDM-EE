package BattleShipApp.service

import BattleShipApp.domain.GameGridFleet
import BattleShipApp.domain.InputGridFleet
import BattleShipApp.domain.InputUserShipName
import BattleShipApp.repository.GameGridFleetRepository
import BattleShipApp.repository.Repositories
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction

class GameGridFleetLogic(
    private val dataBase: Data,
) {
    companion object {
        val gridFleetRep = GameGridFleetRepository()
        fun incrementQuantity(transaction: Transaction, userIdAndShipName: InputUserShipName): GameGridFleet {
            val list = gridFleetRep.getGridFleetsById(transaction, userIdAndShipName.userId)
                .filter { it.shipName == userIdAndShipName.shipName }
            val quantity = list.first().quantity
            return gridFleetRep.updateGridFleet(
                transaction,
                InputGridFleet(userIdAndShipName.userId, userIdAndShipName.shipName, quantity + 1)
            )
        }

        fun decrementQuantity(transaction: Transaction, userIdAndShipName: InputUserShipName): GameGridFleet {
            val list = gridFleetRep.getGridFleetsById(transaction, userIdAndShipName.userId)
                .filter { it.shipName == userIdAndShipName.shipName }
            val quantity = list.first().quantity
            return gridFleetRep.updateGridFleet(
                transaction,
                InputGridFleet(userIdAndShipName.userId, userIdAndShipName.shipName, quantity - 1)
            )
        }
    }

    fun deleteGridFleets(transaction: Transaction, userId: Int, ships: List<String>) =
        gridFleetRep.deleteGridFleet(transaction, userId, ships)

}