package BattleShipApp.domain

data class Authors(
    val authors: List<String>,
    val version: Double
)

fun createMyAuthors():Authors{
    return Authors(listOf("Kaiwei","Tiago"), 0.1)
}