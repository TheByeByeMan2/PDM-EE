package BattleShipApp.domain

import java.sql.Timestamp


data class WaitingRoom(
    val userId: Int,
    val username: String,
    val gameId: Int?,
    val isGo: Boolean,
    val time: Timestamp,
    val ruleId: Int,
)