package pdm.battleshipApp.gameModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import pdm.battleshipApp.battleShip.model.Boards
import pdm.battleshipApp.battleShip.model.Game
import pdm.battleshipApp.battleShip.model.GState
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.ShipType
/*
class GameState(private val scope: CoroutineScope, val player: Player) {
    var game by mutableStateOf(Game(player, player.other()))
    var state by mutableStateOf(GState.BUILDING)
    var currentShipType by mutableStateOf(ShipType.values.first())
    var currentDirection by mutableStateOf(Direction.values().first())
    var hasClick by mutableStateOf(false)
    val hasClickOnBoard: () -> Unit = {
        hasClick = !hasClick
    }
    val onClick:(Position)->Unit = {
        hasClickOnBoard()
        if (state === GState.BUILDING){
            val ship = game.boards.myBoard.getShipInFleet(it)
            if (ship == null) {
                game = game.putFleetMyBoard(it, currentShipType, currentDirection)
            }
            else game = game.removeFleetMyBoard(it)
        }
/*
        if (state === GState.BATTLE){
            val ship = game.boards.other.getShipInFleet(it)
            game = if (ship == null) game.otherBoardMiss(it)
            else game.shotOtherBoard(it)
        }

 */
    }

}



 */