package pdm.battleshipApp.battleShip.model

import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.Ship
import pdm.battleshipApp.battleShip.model.ship.ShipState
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.exceptions.InvalidPutException

enum class GridCellState {
    WATER, MISS, SHIP, SHOT, SINK
}

enum class Action {
    PUT, REMOVE
}

data class Boards(
    val myBoard: MyBoard = MyBoard(),
    val other: EnigmaBoard = EnigmaBoard()
) {
}

fun initialRemainShips(): Map<ShipType, Int> {
    val map = mutableMapOf<ShipType, Int>()
    ShipType.values.forEach {
        map.put(it, it.fleetQuantity)
    }
    return map
}

data class EnigmaBoard(val grid: Map<Position, GridCellState> = mapOf()){
    fun updateGrid(pos: Position, state: GridCellState): EnigmaBoard{
        val newG = grid.toMutableMap()
        newG[pos] = state
        return this.copy(newG.toMap())
    }
}

data class MyBoard(
    private val fleet: Map<Position, Ship> = mapOf(),
    private val missList: List<Position> = listOf(),
    private val remainShips: Map<ShipType, Int> = initialRemainShips(),
) {
    fun incOrDecQuantity(shipType: ShipType, action: Action): MyBoard {
        val value = if (action === Action.PUT) -1
        else +1
        val resMap = remainShips as MutableMap
        val quantity = remainShips.get(shipType)
        if (quantity == 0 && action === Action.PUT) throw InvalidPutException("This ship no more to put")
        if (quantity != null) {
            resMap[shipType] = quantity + value
        } else throw InvalidPutException("This shipType ${shipType.name} not exists in remainShip")
        return this.copy(remainShips = resMap)
    }

    val getRemainShip get() = remainShips

    fun putFleet(pos: Position, value: Ship): MyBoard {
        val resFleet = fleet.toMutableMap()
        resFleet[pos] = value
        return this.copy(fleet = resFleet)
    }

    fun getShipInFleet(pos: Position): Ship? = fleet.get(pos)
    fun getFleet() = fleet
    fun removeFleet(pos: Position): MyBoard {
        val resFleet = fleet.toMutableMap()
        resFleet.remove(pos)
        return this.copy(fleet = resFleet)
    }

    fun shotShip(pos: Position): MyBoard {
        val getShip = getShipInFleet(pos)
        return putFleet(pos, getShip!!.copy(shipState = ShipState.SHOT))
    }

    fun sinkShip(pos: Position): MyBoard {
        val getShip = getShipInFleet(pos)
        return putFleet(pos, getShip!!.copy(shipState = ShipState.SINK))
    }

    fun shotMiss(pos: Position): MyBoard {
        val newMissList = missList.toMutableList()
        newMissList.add(pos)
        return this.copy(missList = missList)
    }

    fun getMissList() = missList

    fun shipStateToGridCellState(pos: Position): GridCellState {
        val shipState = fleet[pos]?.shipState
        val gridCellState = GridCellState.values().find { it.name == shipState?.name }
        if (gridCellState == null) {
            if (shipState === ShipState.ALIVE) return GridCellState.SHIP
            val isMiss = missList.contains(pos)
            if (isMiss) return GridCellState.MISS
            return GridCellState.WATER
        } else return gridCellState
    }
}
