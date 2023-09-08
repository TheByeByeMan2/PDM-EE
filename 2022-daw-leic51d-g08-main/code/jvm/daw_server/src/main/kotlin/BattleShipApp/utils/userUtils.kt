package BattleShipApp.utils

import BattleShipApp.domain.UserState
import BattleShipApp.errors.SemanticErrorException
import java.util.*

fun newToken(): UUID = UUID.randomUUID()

fun String.toUserStateOrNull() = UserState.values().find { it.name == this.uppercase() }

fun String.toUserState() = this.toUserStateOrNull() ?:throw SemanticErrorException("Cannot convert $this to UserState")