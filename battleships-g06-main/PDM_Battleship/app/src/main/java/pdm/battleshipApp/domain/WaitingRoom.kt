package pdm.battleshipApp.domain

import java.sql.Timestamp

data class WaitingRoomSupport(
    val userId: Int,
    val username: String,
    val gameId: Int?,
    val isGo: Boolean,
    val time: Timestamp,
    val ruleId: Int
)

data class WaitingRoom(
    val userId: Int,
    val username: String,
    val gameId: Int?,
    val ruleId: Int
)

data class WaitingRoomMsg(
    val userId: Int,
    val ruleId: Int
)