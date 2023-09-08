package pdm.battleshipApp.battleShip.model

import pdm.battleshipApp.battleShip.model.coords.*
import pdm.battleshipApp.battleShip.model.ship.Ship
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.domain.GameState
import pdm.battleshipApp.exceptions.InvalidPositionException
import pdm.battleshipApp.gameModel.Player
import java.sql.Timestamp

enum class GState { BUILDING, BATTLE, FINISH }

enum class PlayError { NONE, INVALID, OCCUPIED, GAME_OVER, UNKNOWN }

data class PlayResult(val game: Game, val error: PlayError, val msg: String)

data class Game(
    val gameId: Int,
    val player: Player,
    val otherPlayer: Player,
    val boards: Boards = Boards(),
    val initialTurn: Timestamp?,
    val winner: String?,
    val remainingShot: Int,
    val gameState: GameState,
    val turn: Player,
) {
    init {
        check(player != otherPlayer)
    }

    fun Player.other() = if (this.id == player.id) otherPlayer else player

    fun putFleetMyBoard(position: Position, shipType: ShipType, direction: Direction): Game {
        var boards =
            boards.copy(myBoard = putShiLogic(boards.myBoard, position, direction, shipType))
        boards = boards.copy(myBoard = boards.myBoard.incOrDecQuantity(shipType, Action.PUT))
        return this.copy(boards = boards)
    }

    fun removeFleetMyBoard(position: Position): Game {
        val shipType = this.boards.myBoard.getShipInFleet(position)?.shipType
            ?: throw IllegalStateException("Can not be null in remove ship")
        val newBoard = removeShipLogic(boards.myBoard, position)
        var boards = boards.copy(myBoard = newBoard)
        boards = boards.copy(myBoard = boards.myBoard.incOrDecQuantity(shipType, Action.REMOVE))
        return this.copy(boards = boards)
    }

    fun updateEnigmaGrid(position: Position, gridCellState: GridCellState): Game {
        val nBoards = this.boards.copy(other = this.boards.other.updateGrid(position, gridCellState))
        return this.copy(boards = nBoards)
    }

    fun shotMyShip(position: Position): Game{
        val boards = boards.copy(myBoard = boards.myBoard.shotShip(position))
        return this.copy(boards = boards)
    }

    fun sinkMyShip(positions: List<Position>):Game {
        var boards = boards
        positions.forEach {
            boards = boards.copy(boards.myBoard.sinkShip(it))
        }
        return this.copy(boards = boards)
    }

    /*
        fun buildOtherBoard(fleet: Map<Position, Ship>): Game {
            val myBoard = MyBoard(fleet)
            val newBoards = this.boards.copy(other = myBoard)
            return this.copy(boards = newBoards)
        }


            fun shotOtherBoard(position: Position): Game{
                val newBoards = this.boards.copy(other = boards.other.shotShip(position))
                return this.copy(boards = newBoards)
            }


            fun myBoardMiss(position: Position):Game{
                val newBoards = this.boards.copy(myBoard = boards.myBoard.shotMiss(position))
                return this.copy(boards = newBoards)
            }

            fun otherBoardMiss(position: Position): Game{
                val newBoards = this.boards.copy(other = boards.myBoard.shotMiss(position))
                return this.copy(boards = newBoards)
            }
        */
    private fun removeShipLogic(myBoard: MyBoard, position: Position): MyBoard {
        val ship = myBoard.getShipInFleet(position)
        val head = getShipHeadPos(myBoard, position)
        val positions = calculateShipPositions(head, ship!!.direction, ship.shipType)
        var removeBoard = myBoard
        positions.forEach {
            removeBoard = removeBoard.removeFleet(it)
        }
        return removeBoard
    }

    private fun getShipHeadPos(myBoard: MyBoard, position: Position): Position {
        var col = position.column.ordinal
        var row = position.row.ordinal
        while (myBoard.getShipInFleet(Position[col, row]) != null) {
            col -= 1
            if (col < 0) break
        }
        col += 1
        while (myBoard.getShipInFleet(Position[col, row]) != null) {
            row -= 1
            if (row < 0) break
        }
        row += 1
        return Position[col, row]
    }

    private fun putShiLogic(
        myBoard: MyBoard,
        position: Position,
        direction: Direction,
        shipType: ShipType
    ): MyBoard {
        val list = calculateShipPositions(position, direction, shipType)
        validShipAndAroundPositions(position, list, direction, myBoard)
        val ship = Ship(shipType, position, direction)
        var putBoard = myBoard
        list.forEach {
            putBoard = putBoard.putFleet(it, ship)
        }
        return putBoard
    }

    private fun calculateShipPositions(
        headPos: Position,
        direction: Direction,
        shipType: ShipType
    ): List<Position> {
        val list = mutableListOf<Position>()
        if (direction === Direction.HORIZONTAL) {
            repeat(shipType.squares) {
                val colIndex =
                    if (headPos.column.ordinal + it >= Column.values().size) throw InvalidPositionException()
                    else headPos.column.ordinal + it
                list.add(Position[colIndex, headPos.row.ordinal])
            }
            return list
        } else {
            repeat(shipType.squares) {
                val rowIndex =
                    if (headPos.row.ordinal + it >= Row.values().size) throw InvalidPositionException()
                    else headPos.row.ordinal + it
                list.add(Position[headPos.column.ordinal, rowIndex])
            }
            return list
        }
    }

    private fun validShipAndAroundPositions(
        headPos: Position,
        shipPositions: List<Position>,
        direction: Direction,
        myBoard: MyBoard
    ) {
        val aroundRow = if ((headPos.row.ordinal - 1).indexToRowOrNull() == null) headPos.row
        else (headPos.row.ordinal - 1).indexToRow()
        val aroundCol =
            if ((headPos.column.ordinal - 1).indexToColumnOrNull() == null) headPos.column
            else (headPos.column.ordinal - 1).indexToColumn()
        if (direction === Direction.HORIZONTAL) {
            val colLimit = shipPositions.size + 2
            val rowLimit = 3
            var deltaRow = 0
            while (deltaRow < rowLimit) {
                var deltaCol = 0
                while (deltaCol < colLimit) {
                    val colIndex = aroundCol.ordinal + deltaCol
                    val rowIndex = aroundRow.ordinal + deltaRow
                    if (colIndex.indexToColumnOrNull() == null || rowIndex.indexToRowOrNull() == null) break
                    val currentPosition = Position[colIndex, rowIndex]
                    if (myBoard.getShipInFleet(currentPosition) != null) throw InvalidPositionException()
                    deltaCol += 1
                }
                deltaRow += 1
            }
        } else {
            val colLimit = 3
            val rowLimit = shipPositions.size + 2
            var deltaCol = 0
            while (deltaCol < colLimit) {
                var deltaRow = 0
                while (deltaRow < rowLimit) {
                    val colIndex = aroundCol.ordinal + deltaCol
                    val rowIndex = aroundRow.ordinal + deltaRow
                    if (colIndex.indexToColumnOrNull() == null || rowIndex.indexToRowOrNull() == null) break
                    val currentPosition = Position[colIndex, rowIndex]
                    if (myBoard.getShipInFleet(currentPosition) != null) throw InvalidPositionException()
                    deltaRow++
                }
                deltaCol++
            }
        }
    }

}

fun checkTimer(time: String): Boolean =
    when (time) {
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" -> true
        else -> false
    }