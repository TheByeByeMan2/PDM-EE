package pdm.battleshipApp.battleShip.model.ship

val MAX_SHIPS = ShipType.values.sumOf { it.fleetQuantity * it.squares }
val MAX_N_SHIPS = ShipType.values.sumOf { it.fleetQuantity }

/**
 * All ship types allowed in the game.
 * @property name Ship type name.
 * @property squares Number of squares occupied vertically or horizontally.
 * @property fleetQuantity Number of ships of this type in the starting fleet.
 */
class ShipType private constructor(val name: String, val squares:Int, val fleetQuantity:Int) {
    companion object {
        val values = listOf( ShipType("Carrier",5,1), ShipType("Battleship",4,2),
            ShipType("Cruiser",3,3), ShipType("Submarine",2,4) )
    }
}

fun Int.toShipTypeOrNull() = ShipType.values.find { it.squares == this }

fun Int.toShipType() = this.toShipTypeOrNull() ?: throw NoSuchElementException()

fun String.toShipTypeOrNull() = ShipType.values.find {
    (it.squares.toString() == this) or it.name.uppercase().startsWith(this.uppercase()).also {
        if (equals("C")) return null
    }
}

fun String.toShipType() = this.toShipTypeOrNull()?: throw NoSuchElementException("Invalid ship type $this")
