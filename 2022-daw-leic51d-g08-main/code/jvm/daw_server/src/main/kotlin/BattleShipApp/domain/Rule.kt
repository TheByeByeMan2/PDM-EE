package BattleShipApp.domain

data class Rule(
    val ruleId: Int,
    val gridSize: String,
    val shotNumber: Int,
    val timeout: Int,
)

data class RuleAndShipType(
    val rule: Rule, val shipTypes: List<ShipType>
)