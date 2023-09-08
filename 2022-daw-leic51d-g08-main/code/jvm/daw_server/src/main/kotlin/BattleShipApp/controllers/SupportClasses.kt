package BattleShipApp.controllers

import BattleShipApp.domain.GridCell
import BattleShipApp.domain.InputCreateRule
import BattleShipApp.utils.Position
import BattleShipApp.utils.ShipState

data class UserId(val userId: Int)

enum class PutRemove{
    PUT, REMOVE
}
data class UserPutOrRemoveShip(val userId: Int, val shipName: String?, val position: Position, val direction: String, val action: String)

data class PutShipSupport( val shipName: String, val position: Position, val direction: String)
data class PutShips(val userId: Int, val gameId: Int, val ships: List<PutShipSupport>)

data class UserIdAndPosition(val userId: Int, val position: Position)

data class InputCreateShipType(val shipName: String,
                               val squares: Int,
                               val fleetQuantity: Int)
data class CreateRuleAndShipTypes(val rule: InputCreateRule, val shipTypes: List<InputCreateShipType>)

enum class ShotState{
    MISS, SHOT
}
data class ShotResult(val shotState: ShotState, val gridCells: List<GridCell>?)