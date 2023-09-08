package pdm.battleshipApp.battleShip.model.utils

import pdm.battleshipApp.battleShip.model.GridCellState
import pdm.battleshipApp.battleShip.model.ship.ShipState

fun ShipState.toGridCellState(): GridCellState{
    val gridCellState = GridCellState.values().find { it.name == this.name }
    return if (gridCellState == null && this  === ShipState.ALIVE) GridCellState.SHIP
    else gridCellState ?: throw IllegalStateException("Can not convert ${this.name} to GridState")
}