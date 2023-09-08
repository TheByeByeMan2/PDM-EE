package pdm.battleshipApp.utils

import androidx.compose.ui.text.toLowerCase
import pdm.battleshipApp.battleShip.model.Action
import pdm.battleshipApp.battleShip.model.Game
import pdm.battleshipApp.battleShip.model.MyBoard
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.Ship
import pdm.battleshipApp.battleShip.model.ship.ShipState
import pdm.battleshipApp.battleShip.model.ship.toShipType
import pdm.battleshipApp.domain.*
import pdm.battleshipApp.gameModel.Player

fun String.toPutRemove(): Action {
    return this.toPutRemoveOrNull()
        ?: throw IllegalStateException("Cannot convert this string $this to PutRemove")
}

fun String.toPutRemoveOrNull(): Action? =
    Action.values().find { it.name == this.toUpperCase() }

fun Position.toSupportPosition(): SupportPosition =
    SupportPosition(this.column.ordinal + 1, this.row.ordinal + 1)

fun PutResponse_GridCell.toBoard(headPosition: Position, direction: Direction) = MyBoard(
    mapOf(
        Pair(
            Position[this.column, this.row],
            Ship(this.shipName!!.toShipType(), headPosition, direction)
        )
    )
)


/**
 * Save the first game board
 */
fun Game.copyNonBoard(game: Game): Game = game.copy(boards = this.boards)

fun String.toShipState(): ShipState = ShipState.values().find { it.name.toLowerCase() == this.lowercase() } ?:throw IllegalStateException("The $this can not be ship state")

fun ServerGame.toGameAndSaveBoards(game: Game) = game.copy(
    gameId = this.gameId,
    player = Player(this.userA),
    otherPlayer = Player(this.userB),
    initialTurn = this.initialTurn,
    winner = this.winner,
    remainingShot = this.remainingShot,
    gameState = this.gameState,
    turn = Player(this.turn)
)

fun Position.toServerPosition() = ServerPosition(this.column.ordinal+1, this.row.ordinal+1)