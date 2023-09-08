package pdm.battleshipApp

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import pdm.battleshipApp.battleShip.model.Game
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.Ship
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.exceptions.InvalidPositionException
import pdm.battleshipApp.exceptions.InvalidPutException
import pdm.battleshipApp.gameModel.Player


class ServerGameMyBoardTest {
    /*
    @Test
    fun put_one_ship_on_board() {
        var game = Game(Player.PLAYER_A, Player.PLAYER_B)
        val pos = Position[1,1]
        val shipType = ShipType.values.first()
        val direction = Direction.HORIZONTAL
        game = game.putFleetMyBoard(pos, shipType, direction)
        val ship = Ship(shipType, pos, direction)
        repeat(shipType.squares){
            assertEquals(ship, game.boards.myBoard.getShipInFleet(Position[1+it,1]))
        }
        assertNull(game.boards.myBoard.getShipInFleet(Position[6,1]))
        assertEquals(shipType.fleetQuantity-1, game.boards.getRemainShip().get(shipType))
    }

    @Test
    fun put_two_ships_on_board(){
        var game = Game(Player.PLAYER_A, Player.PLAYER_B)
        val pos = Position[1,1]
        val pos2 = Position[1,3]
        val shipType = ShipType.values.first()
        val shipType2 = ShipType.values.get(1)
        val direction = Direction.HORIZONTAL
        game = game.putFleetMyBoard(pos, shipType, direction)
        game = game.putFleetMyBoard(pos2, shipType2, direction)
        val ship = Ship(shipType, pos, direction)
        val ship2 = Ship(shipType2, pos2, direction)
        repeat(shipType.squares){
            assertEquals(ship, game.boards.myBoard.getShipInFleet(Position[1+it,1]))
        }
        repeat(shipType2.squares){
            assertEquals(ship2, game.boards.myBoard.getShipInFleet(Position[1+it,3]))
        }
        assertNull(game.boards.myBoard.getShipInFleet(Position[6,1]))
        assertNull(game.boards.myBoard.getShipInFleet(Position[5,3]))
        assertEquals(shipType.fleetQuantity-1, game.boards.getRemainShip().get(shipType))
        assertEquals(shipType2.fleetQuantity-1, game.boards.getRemainShip().get(shipType2))
    }

    @Test
    fun put_one_ship_out_of_board(){
        var game = Game(Player.PLAYER_A, Player.PLAYER_B)
        val pos = Position[9,9]
        val shipType = ShipType.values.first()
        val direction = Direction.HORIZONTAL
        assertThrows(InvalidPositionException::class.java) {
            game = game.putFleetMyBoard(pos, shipType, direction)
        }
    }

    @Test
    fun put_tow_ship_on_board_with_conflict(){
        var game = Game(Player.PLAYER_A, Player.PLAYER_B)
        val pos1 = Position[1,2]
        val pos2 = Position[1,1]
        val shipType = ShipType.values.get(1)
        val direction = Direction.HORIZONTAL
        game = game.putFleetMyBoard(pos1, shipType, direction)
        println()
        assertThrows(InvalidPositionException::class.java) {
            game = game.putFleetMyBoard(pos2, shipType, direction)
        }
        assertEquals(shipType.fleetQuantity-1, game.boards.getRemainShip().get(shipType))
    }
     */
}