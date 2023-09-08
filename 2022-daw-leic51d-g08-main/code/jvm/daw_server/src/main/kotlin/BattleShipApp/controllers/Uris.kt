package BattleShipApp.controllers

object Uris {

    const val HOME = "api/"
    const val AUTHORS = "api/authors"
    const val RANKING = "api/ranking"
    const val REGISTER = "api/register"
    const val LOGIN = "api/login"
    const val WAITING = "api/games/waiting"
    const val WAITING_EXIT = "api/games/waiting/exit"
    const val BUILDING = "api/games/{id}/building"
    const val BUILDING_ALL = "api/games/{id}/building_all"
    const val SET_READY = "api/games/{id}/isReady"
    const val BATTLE = "api/games/{id}/battle"
    const val END = "api/games/{id}/end"
    const val BATTLE_EXIT = "api/games/{id}/exit"
    const val GAME_INFO = "api/games/{id}/info"
    const val USER_INFO = "api/user/{id}"
    const val USER_COMPLEX_INFO = "api/user/special/{id}"
    const val ALL_RULES = "api/rules"
    const val RULE = "api/rules/{ruleId}"

}