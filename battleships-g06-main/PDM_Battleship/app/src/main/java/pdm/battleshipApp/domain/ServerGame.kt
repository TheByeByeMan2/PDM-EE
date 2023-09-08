package pdm.battleshipApp.domain

import pdm.battleshipApp.battleShip.model.Action
import pdm.battleshipApp.battleShip.model.Game
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.gameModel.Player
import java.sql.Timestamp

enum class GameState { START, BATTLE, END }

data class ServerSupportGame(
    val gameId: Int,
    val userA: Int,
    val userB: Int,
    val turn: Int,
    val initialTurn: Timestamp?,
    val readyA: Boolean,
    val readyB: Boolean,
    val winner: String?,
    val finishA: Boolean,
    val finishB: Boolean,
    val ruleId: Int,
    val remainingShot: Int,
    val gameState: GameState
)

data class ServerGame(
    val gameId: Int,
    val userA: Int,
    val userB: Int,
    val turn: Int,
    val initialTurn: Timestamp?,
    val winner: String?,
    val remainingShot: Int,
    val gameState: GameState
)

data class PutOrRemoveShip(
    val shipType: ShipType,
    val position: Position,
    val direction: Direction,
    val action: Action
)

data class SupportPosition(val col: Int, val row: Int)

data class SupportUserPutOrRemoveShip(
    val userId: Int,
    val shipName: String?,
    val position: SupportPosition,
    val direction: String,
    val action: String
)

data class PutResponse_GridCell(
    val gameId: Int,
    val userId: Int,
    val column: Int,
    val row: Int,
    val shipState: String?,
    val shipName: String?
)

fun ServerSupportGame.toServerGame() = ServerGame(
    this.gameId,
    this.userA,
    this.userB,
    this.turn,
    this.initialTurn,
    this.winner,
    this.remainingShot,
    this.gameState
)

fun ServerGame.toGame() = Game(
    gameId = this.gameId,
    player = Player(this.userA),
    otherPlayer = Player(this.userB),
    initialTurn = this.initialTurn,
    winner = this.winner,
    gameState = this.gameState,
    turn = Player(this.turn),
    remainingShot = this.remainingShot
)

data class ShipPosDirection(val shipType: ShipType,val position: Position, val direction: Direction)

fun List<ShipPosDirection>.toListPutOrRemoveShip(): List<PutOrRemoveShip>{
    val list = mutableListOf<PutOrRemoveShip>()
    this.forEach {
        list.add(PutOrRemoveShip(it.shipType, it.position, it.direction, Action.PUT))
    }
    return list
}

enum class ShotState{
    MISS, SHOT
}

data class GridCell(
    val gameId: Int,
    val userId: Int,
    val column: Int,
    val row: Int,
    val shipState: String,
    val shipName: String,
)

data class ShotResult(val shotState: ShotState, val gridCells: List<GridCell>?)

data class ServerPosition(val col: Int, val row : Int)

data class PutShipSupport( val shipName: String, val position: ServerPosition, val direction: String)

data class PutShips(val userId: Int, val gameId: Int, val ships: List<PutShipSupport>)

data class InputUserIdGameId(val userId: Int, val gameId: Int)

data class TimeoverRes(val res: Boolean)