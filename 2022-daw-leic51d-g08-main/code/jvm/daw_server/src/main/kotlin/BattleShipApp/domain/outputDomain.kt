import java.util.*


/**
 * Gathers the game info from database
 * @param gameId the id of the game
 * @param userA the id of the user a
 * @param userB the id of the user b
 * @param initialTurn the time that player has been started
 * @param readyA if the user a is ready
 * @param readyB if the user b is ready
 * @param finishA if the user a has been concluded the game
 * @param finishB if the user b has been concluded the game
 */



data class OutPutDeleteGridCell(
    val gameId: Int,
    val userId: Int,
    val column: Int,
    val row: Int,
)




data class OutPutTokenUser(
    val userId: Int,
    val token: UUID,
)