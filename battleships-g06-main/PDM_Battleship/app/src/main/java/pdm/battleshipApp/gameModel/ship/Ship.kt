package pdm.battleshipApp.battleShip.model.ship

import java.lang.IllegalArgumentException
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position

enum class ShipState {
    ALIVE, SHOT, SINK
}

data class Ship(val shipType: ShipType, val headPosition: Position, val direction: Direction, val shipState: ShipState = ShipState.ALIVE){

    /*
    fun body(): List<Position> {
        return if (direction == Direction.VERTICAL)
            bodyCheck(position.row.ordinal + shipType.squares, position, direction, true)
        else
            bodyCheck(position.column.ordinal + shipType.squares, position, direction, false)

    }

    private fun bodyCheck(shipInt: Int, position: Position, direction: Direction, bool: Boolean): List<Position> {
        val body = mutableListOf<Position>()
        if (shipInt > ROW_DIM)
            return body
        for (i in 0 until shipType.squares) {
            if(bool)
                body.add(Position[position.column.ordinal, position.row.ordinal + i])
            else
                body.add(Position[position.column.ordinal + i, position.row.ordinal])

        }
        return body
    }

     */
}

fun changeShipState(ship: Ship, shipState: ShipState) = Ship(ship.shipType, ship.headPosition, ship.direction, shipState)

fun String.toDirectionOrNull(): Direction? = Direction.values().find { it.name.startsWith(this.uppercase()) }

fun String.toDirection() = this.toDirectionOrNull() ?: throw IllegalArgumentException("Invalid direction $this")