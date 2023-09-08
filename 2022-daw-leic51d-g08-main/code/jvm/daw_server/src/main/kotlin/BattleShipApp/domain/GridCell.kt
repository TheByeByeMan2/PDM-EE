package BattleShipApp.domain

/**
 * Gathers the grid info from database
 * @param gameId the id of the game
 * @param userId the id of the user
 * @param column the column coords of the grid
 * @param row the row coords of the grid
 * @param shipState the state of the ship
 */
data class GridCell(
    val gameId: Int,
    val userId: Int,
    val column: Int,
    val row: Int,
    val shipState: String,
    val shipName: String,
)