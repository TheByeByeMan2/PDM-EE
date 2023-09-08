package BattleShipApp.utils

enum class GameState {START, BATTLE, END}

fun String.toGameState() = GameState.values().find { it.name == this.toUpperCase() }
    ?: throw IllegalStateException("Cannot convert this string $this to GameState")