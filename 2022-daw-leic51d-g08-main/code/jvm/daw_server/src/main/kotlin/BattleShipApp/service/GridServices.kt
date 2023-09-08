package BattleShipApp.service

import BattleShipApp.controllers.PutShips
import BattleShipApp.domain.*
import BattleShipApp.errors.FailException
import BattleShipApp.errors.SemanticErrorException
import BattleShipApp.repository.*
import BattleShipApp.service.GameGridFleetLogic.Companion.decrementQuantity
import BattleShipApp.service.GameGridFleetLogic.Companion.incrementQuantity
import BattleShipApp.transactions.Data
import BattleShipApp.transactions.Transaction
import BattleShipApp.utils.*
import OutPutDeleteGridCell
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GridServices(
    private val dataBase: Data,
    private val rep: Repositories,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private fun createNewGrid(
        transaction: Transaction,
        gridInfo: InputGrid,
        state: String = ShipState.ALIVE.name
    ): GridCell {
        return rep.gridRep.createGridCell(transaction, gridInfo, state)
    }

    private fun listGridCreate(
        transaction: Transaction,
        listGrid: List<InputGrid>,
        state: String = ShipState.ALIVE.name
    ): List<GridCell> {
        val list = mutableListOf<GridCell>()
        listGrid.forEach {
            list.add(createNewGrid(transaction, it, state))
        }
        return list
    }

    private fun shipOccupiedHeadPos(shipPosition: Position): Position {
        val c = if (shipPosition.col - 1 > 0) shipPosition.col - 1 else shipPosition.col
        val r = if (shipPosition.row - 1 > 0) shipPosition.row - 1 else shipPosition.row
        return Position(c, r)
    }

    private fun realShipOccupiedHeadPos(shipPosition: Position): Position {
        return Position(shipPosition.col - 1, shipPosition.row - 1)
    }


    private fun shipPositions(
        head: Position,
        direction: Direction,
        shipType: ShipType,
        gridSize: String
    ): List<Position> {
        val split = gridSize.split("x")
        val maxCol = split[0].toInt()
        val maxRow = split[1].toInt()
        val list = mutableListOf<Position>()
        if (direction === Direction.HORIZONTAL) {

            repeat(shipType.squares) {
                val col = if (head.col + it> maxCol) throw FailException("Position Invalid")
                else head.col + it
                list.add(Position(col, head.row))

            }
            return list
        } else {
            repeat(shipType.squares) {
                val row = if (head.row + it> maxRow) throw FailException("Position Invalid")
                else head.row + it
                list.add(Position(head.col, row))
            }
            return list
        }
    }

    /**
     * Valid ship around position
     */
    private fun validPosition(
        shipHeadPos: Position,
        validHeadPos: Position,
        shipType: ShipType,
        direction: Direction,
        gridSize: String,
        playerGridCell: List<GridCell>
    ) {
        val split = gridSize.split("x")
        val maxCol = split[0].toInt()
        val maxRow = split[1].toInt()
        val betaCol = if (shipHeadPos != validHeadPos) validHeadPos.col - shipHeadPos.col else 0
        val betaRow = if (shipHeadPos != validHeadPos) validHeadPos.row - shipHeadPos.row else 0
        if (direction === Direction.HORIZONTAL) {
            var deltaRow = 0
            while (deltaRow < 3 - betaRow) {
                var deltaCol = 0
                val row = if (validHeadPos.row + deltaRow > maxRow) break
                else validHeadPos.row + deltaRow
                while (deltaCol < shipType.squares + 2 - betaCol) {
                    val col = if (validHeadPos.col + deltaCol > maxCol) break
                    else validHeadPos.col + deltaCol
                    if (playerGridCell.find { it.row == row && it.column == col } != null) throw IllegalStateException("Invalid Position")
                    deltaCol += 1
                }
                deltaRow += 1
            }
        } else {
            var deltaCol = 0
            while (deltaCol < 3 - betaCol) {
                var deltaRow = 0
                val col = if (validHeadPos.col + deltaCol > maxCol) break
                else validHeadPos.col + deltaCol
                while (deltaRow < shipType.squares + 2 - betaRow) {
                    val row = if (validHeadPos.row + deltaRow > maxRow) break
                    else validHeadPos.row + deltaRow
                    if (playerGridCell.find { it.row == row && it.column == col } != null) throw IllegalStateException("Invalid Position")
                    deltaRow += 1
                }
                deltaCol += 1
            }

        }
    }

    /**
     * return true if this ship is limit of quantity else false
     *
     */
    private fun validShipLimit(transaction: Transaction, gridInfo: InputGrid): Boolean {
        val gameGridFleet = rep.gameGridFleetRep.getGridFleetByIdAndShipName(
            transaction,
            InputUserShipName(gridInfo.userId, gridInfo.shipName)
        )
        return gameGridFleet.quantity == 0
    }

    private fun putShipOnGrid(transaction: Transaction, gridInfo: InputGrid, direction: Direction): List<GridCell> {
        val shipName = gridInfo.shipName
        val res = mutableListOf<GridCell>()
        val rule = rep.gamesRep.getGameById(transaction, gridInfo.gameId)
        val gridSize = rep.rulesRep.getRule(transaction, rule.ruleId).gridSize
        val shipInfo =
            rep.shipTypeRep.getShipTypeByRuleIdAndShipName(transaction, InputGetShipType(rule.ruleId, shipName))

        val listShipPosition =
            shipPositions(Position(gridInfo.col, gridInfo.row), direction, shipInfo, gridSize)
        val inputGridShips = mutableListOf<InputGrid>()
        listShipPosition.forEach { pos ->
            inputGridShips.add(
                InputGrid(
                    gridInfo.gameId,
                    gridInfo.userId,
                    pos.col,
                    pos.row,
                    shipName
                )
            )
        }
        val playerGrid = rep.gridRep.getGridInfo(transaction, InputGetGridByGameIdAndUserId(gridInfo.gameId, gridInfo.userId))
        validPosition(
            realShipOccupiedHeadPos(Position(gridInfo.col, gridInfo.row)),
            shipOccupiedHeadPos(Position(gridInfo.col, gridInfo.row)),
            shipInfo,
            direction,
            gridSize,
            playerGrid
        )
        res += listGridCreate(transaction, inputGridShips)
        decrementQuantity(transaction, InputUserShipName(gridInfo.userId, shipName))
        return res
    }

    private fun putGridShipLogic(
        transaction: Transaction,
        gridInfo: InputGrid,
        direction: Direction
    ): List<GridCell> {
        return if (!validShipLimit(transaction, gridInfo)) putShipOnGrid(transaction, gridInfo, direction)
        else throw FailException("No more ${gridInfo.shipName} to put")
    }

    fun createNewGridTrans(gridInfo: InputGrid, direction: String): List<GridCell> {
        val trans = dataBase.createTransaction()
        checkDirection(direction)
        val d = direction.toDirection()
        noNegative(gridInfo.userId, "user id")
        noNegative(gridInfo.col, "col")
        noNegative(gridInfo.row, "row")
        noNegative(gridInfo.gameId, "game id")
        noEmptyString(gridInfo.shipName, "ship name")
        return trans.executeTransaction<List<GridCell>>(trans) {
            inconvenientWinner(it, gridInfo.gameId)
            checkUserBattleState(it, gridInfo.userId)
            checkStartGameState(it, gridInfo.gameId)
            checkGameById(it, gridInfo.gameId)
            checkUserById(it, gridInfo.userId)
            checkShipByNameAndGameId(it, gridInfo.shipName, gridInfo.gameId)
            putGridShipLogic(it, gridInfo, d)
        }
    }

    fun createPutShipsTrans(inpuShips: PutShips): List<GridCell> {
        val trans = dataBase.createTransaction()
        inpuShips.ships.forEach {
            checkDirection(it.direction)
            it.direction.toDirection()
            noNegative(it.position.col, "col")
            noNegative(it.position.row, "row")
            noEmptyString(it.shipName, "ship name")
        }
        noNegative(inpuShips.gameId, "game id")
        noNegative(inpuShips.userId, "user id")
        return trans.executeTransaction<List<GridCell>>(trans) {tr->
            inconvenientWinner(tr, inpuShips.gameId)
            checkUserBattleState(tr, inpuShips.userId)
            checkStartGameState(tr, inpuShips.gameId)
            checkGameById(tr, inpuShips.gameId)
            checkUserById(tr, inpuShips.userId)
            var res: List<GridCell> = mutableListOf()
            inpuShips.ships.forEach {
                val d = it.direction.toDirection()
                checkShipByNameAndGameId(tr, it.shipName, inpuShips.gameId)
                res = res + putGridShipLogic(tr, InputGrid(inpuShips.gameId, inpuShips.userId, it.position.col, it.position.row, it.shipName), d)
            }
            res
        }
    }

    private fun shipHeadPosition(transaction: Transaction, gridInfo: InputGrid): Position {
        var col = gridInfo.col
        var row = gridInfo.row
        while (rep.gridRep.getCellInfo(transaction, gridInfo.toInputGridCellInfo(col = col, row = row)) != null) {
            col -= 1
        }
        col += 1
        while (rep.gridRep.getCellInfo(transaction, gridInfo.toInputGridCellInfo(col = col, row = row)) != null) {
            row -= 1
        }
        row += 1
        return Position(col, row)
    }

    private fun findShipDirection(transaction: Transaction, gridInfo: InputGrid): Direction {
        val col = gridInfo.col + 1
        return if (rep.gridRep.getCellInfo(transaction, gridInfo.toInputGridCellInfo(col = col)) != null) {
            Direction.HORIZONTAL
        } else Direction.VERTICAL
    }

    private fun removeShip(transaction: Transaction, removeInfo: InputGridNonShipName): List<OutPutDeleteGridCell> {
        val res = mutableListOf<OutPutDeleteGridCell>()
        val shipName = rep.gridRep.getCellInfo(transaction, removeInfo)!!.shipName
        val gridInfo = InputGrid(removeInfo.gameId, removeInfo.userId, removeInfo.col, removeInfo.row, shipName)
        val head = shipHeadPosition(transaction, gridInfo)
        val direction = findShipDirection(transaction, gridInfo)
        val ruleId = rep.gamesRep.getGameById(transaction, gridInfo.gameId).ruleId
        val gridSize = rep.rulesRep.getRule(transaction, ruleId).gridSize
        val shipType =
            rep.shipTypeRep.getShipTypeByRuleIdAndShipName(transaction, InputGetShipType(ruleId, gridInfo.shipName))
        val shipPositions = shipPositions(head, direction, shipType, gridSize)
        shipPositions.forEach {
            res.add(
                rep.gridRep.deleteGridCell(
                    transaction,
                    InputGridNonShipName(gridInfo.gameId, gridInfo.userId, it.col, it.row)
                )
            )
        }
        return res
    }

    private fun removeShipLogic(
        transaction: Transaction,
        removeInfo: InputGridNonShipName
    ): List<OutPutDeleteGridCell> {
        try {
            verifyCell(transaction, removeInfo)
            val shipName = rep.gridRep.getCellInfo(transaction, removeInfo)!!.shipName
            val res = removeShip(transaction, removeInfo)
            incShipQuantity(transaction, removeInfo.userId, shipName)
            return res
        } catch (e: Exception) {
            throw e
        }
    }

    private fun verifyCell(transaction: Transaction, removeInfo: InputGridNonShipName) {
        if (rep.gridRep.getCellInfo(transaction, removeInfo) == null) throw IllegalStateException("Invalid Position")
    }

    private fun incShipQuantity(transaction: Transaction, userId: Int, shipName: String) {
        incrementQuantity(transaction, InputUserShipName(userId, shipName))
    }

    fun removeShipTrans(removeInfo: InputGridNonShipName): List<OutPutDeleteGridCell> {
        val trans = dataBase.createTransaction()
        noNegative(removeInfo.userId, "user id")
        noNegative(removeInfo.col, "col")
        noNegative(removeInfo.row, "row")
        noNegative(removeInfo.gameId, "game id")
        return trans.executeTransaction<List<OutPutDeleteGridCell>>(trans) {
            inconvenientWinner(it, removeInfo.gameId)
            checkUserBattleState(it, removeInfo.userId)
            checkStartGameState(it, removeInfo.gameId)
            checkGameById(it, removeInfo.gameId)
            checkUserById(it, removeInfo.userId)
            removeShipLogic(it, removeInfo)
        }
    }

    /**
     *
     *
     */
    private fun updateGrid(transaction: Transaction, grid: InputUpdateGridShipState): GridCell {
        if (grid.userId <= 0) throw SemanticErrorException("The user a id cannot be negative")
        if (grid.gameId <= 0) throw SemanticErrorException("The gameId id cannot be negative")
        if (grid.col <= 0) throw SemanticErrorException("The row coords cannot be negative")
        if (grid.row <= 0) throw SemanticErrorException("The row coords cannot be negative")
        if (!rep.usersRep.checkIfUserIdExists(
                transaction,
                grid.userId
            )
        ) throw SemanticErrorException("The user id not exist")
        if (!rep.gamesRep.checkIfGameIdExists(
                transaction,
                grid.gameId
            )
        ) throw SemanticErrorException("The game id not exist")
        return rep.gridRep.updateGridCell(transaction, grid)

    }

    private fun getGridInfo(transaction: Transaction, grid: InputGetGridByGameIdAndUserId): List<GridCell> {
        return rep.gridRep.getGridInfo(transaction, grid)
    }

    fun getGridInfoTrans(grid: InputGetGridByGameIdAndUserId): List<GridCell> {
        val trans = dataBase.createTransaction()
        noNegative(grid.gameId, "game id")
        noNegative(grid.userId, "user id")
        return trans.executeTransaction<List<GridCell>>(trans) {
            checkUserBattleState(it, grid.userId)
            checkGameById(it, grid.gameId)
            checkUserById(it, grid.userId)
            getGridInfo(it, grid)
        }
    }


    fun shotGrid(transaction: Transaction, input: InputGridNonShipName): GridCell? {
        val gameInfo = rep.gamesRep.getGameById(transaction, input.gameId)
        val otherPlayer = if (gameInfo.userA == input.userId) gameInfo.userB
        else gameInfo.userA
        if (gameInfo.turn == input.userId) {
            try {
                val otherGridInfo = getGridInfo(transaction, InputGetGridByGameIdAndUserId(input.gameId, otherPlayer))
                val otherCell =
                    otherGridInfo.filter {
                        it.gameId == input.gameId &&
                                it.userId == otherPlayer &&
                                it.row == input.row &&
                                it.column == input.col
                    }
                if (otherCell.isEmpty()) return null

                val shipState = otherCell.first().shipState.toShipState()
                if (shipState === ShipState.ALIVE) {
                    return updateGrid(
                        transaction,
                        InputUpdateGridShipState(input.gameId, otherPlayer, input.col, input.row, ShipState.SHOT.name)
                    )
                } else throw IllegalStateException("Shot in position invalid")
            } catch (e: Exception) {
                throw e
            }
        }
        throw IllegalStateException("Is not your turn")
    }

    fun miss(transaction: Transaction, remainingShot: Int, gameId: Int, initShotNumber: Int) {
        val shot = remainingShot - 1
        val newGameInfo = rep.gamesRep.updatePerShot(transaction, InputUpdatePerShot(gameId, shot))
        if (newGameInfo.remainingShot == 0) {
            rep.gamesRep.switchTurn(transaction, gameId)
            rep.gamesRep.updatePerShot(transaction, InputUpdatePerShot(gameId, initShotNumber))
        }
    }

    fun checkWinner(gridCell: List<GridCell>) =
        gridCell.find { it.shipState != ShipState.SINK.name } == null

    fun getOtherPlayer(transaction: Transaction, player: Int, gameId: Int): Int {
        val game = rep.gamesRep.getGameById(transaction, gameId)
        return if (game.userA == player) game.userB
        else game.userA
    }


    fun sinkShipTrans(
        other: InputGridNonShipName,
        rule: Rule
    ): MutableList<GridCell> {
        val trans = dataBase.createTransaction()
        return trans.executeTransaction(trans) {
            sinkShip(it, other, rule)
        }
    }

    fun sinkShip(
        transaction: Transaction,
        other: InputGridNonShipName,
        rule: Rule,
    ): MutableList<GridCell> {
        val res = mutableListOf<GridCell>()
        val otherGrid = rep.gridRep.getGridInfo(transaction, InputGetGridByGameIdAndUserId(other.gameId, other.userId))
        val head = calculateShipHeadPos(otherGrid, Position(other.col, other.row), rule)
        val shipType = rep.shipTypeRep.getShipTypeByRuleIdAndShipName(
            transaction,
            InputGetShipType(rule.ruleId, otherGrid.find { it.row == other.row && it.column == other.col }!!.shipName)
        )
        val direction = if (rep.gridRep.getCellInfo(
                transaction,
                other.copy(userId = other.userId, col = head.col + 1, row = head.row)
            ) == null
        ) Direction.VERTICAL
        else Direction.HORIZONTAL
        val positions = mutableListOf<Position>()
        if (direction === Direction.HORIZONTAL) {
            var colDelta = 0
            while (colDelta < shipType.squares) {
                val col = head.col + colDelta
                val row = head.row
                val gridCell =
                    rep.gridRep.getCellInfo(transaction, other.copy(userId = other.userId, col = col, row = row))
                if (gridCell?.shipState != ShipState.SHOT.name) break
                positions.add(Position(col, row))
                colDelta += 1
            }
        } else {
            var rowDelta = 0
            while (rowDelta < shipType.squares) {
                val col = head.col
                val row = head.row + rowDelta
                val gridCell =
                    rep.gridRep.getCellInfo(transaction, other.copy(userId = other.userId, col = col, row = row))
                if (gridCell?.shipState != ShipState.SHOT.name) break
                positions.add(Position(col, row))
                rowDelta += 1
            }
        }
        println(positions)
        if (positions.size == shipType.squares) {
            positions.forEach {
                res.add(
                    rep.gridRep.updateGridCell(
                        transaction,
                        InputUpdateGridShipState(other.gameId, other.userId, it.col, it.row, ShipState.SINK.name)
                    )
                )
            }
        }
        return res
    }

    fun calculateShipHeadPos(gridCell: List<GridCell>, pos: Position, rule: Rule): Position {
        val splint = rule.gridSize.split("x")
        val maxCol = splint[0].toInt()
        val maxRow = splint[1].toInt()
        var currentCol = pos.col
        var currentRow = pos.row
        while (currentCol in 1 .. maxCol) {
            currentCol = currentCol - 1
            val cell =
                gridCell.find { it.column == currentCol && it.row == currentRow }
            if (cell == null) {
                currentCol += 1
                break
            }
        }
        while (currentRow in 1 .. maxRow) {
            currentRow = currentRow - 1
            val cell =
                gridCell.find { it.column == currentCol && it.row == currentRow }
            if (cell == null) {
                currentRow += 1
                break
            }
        }
        return Position(currentCol, currentRow)
    }

}