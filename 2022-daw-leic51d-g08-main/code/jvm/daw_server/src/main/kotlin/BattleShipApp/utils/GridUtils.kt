package BattleShipApp.utils

import BattleShipApp.domain.InputGrid
import BattleShipApp.domain.InputGridNonShipName

enum class ShipState { ALIVE, SHOT, SINK }

enum class Direction{
    HORIZONTAL, VERTICAL
}

data class Position(val col: Int, val row: Int)

fun InputGrid.toInputGridCellInfo(
    gameId: Int = this.gameId,
    userId: Int = this.userId,
    col: Int = this.col,
    row: Int = this.row
): InputGridNonShipName = InputGridNonShipName(gameId, userId, col, row)

fun String.toShipState(): ShipState {
    return ShipState.values().find { it.name == this.toUpperCase() }
        ?: throw IllegalStateException("Cannot convert this string $this to ShipState")
}

fun String.toDirectionOrNull():Direction? = Direction.values().find { it.name == this.toUpperCase() }

fun String.toDirection(): Direction = this.toDirectionOrNull()
        ?: throw IllegalStateException("Cannot convert this string $this to Direction")
