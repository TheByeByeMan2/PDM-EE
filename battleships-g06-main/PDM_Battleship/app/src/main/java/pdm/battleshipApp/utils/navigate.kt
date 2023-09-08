package pdm.battleshipApp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import pdm.battleshipApp.domain.LocalData

enum class ResponseState {
    Success, Failure
}

const val LOGIN_VALUE_RETURN = "login_return"
const val USER_DATA = "USER_DATA"
const val WAITINGROOM_GAMEID = "waitingRoom_gameId"
fun navigate(origin: Activity, dest: Class<*>, data: LocalData? = null, tag: String = USER_DATA) {
    with(origin) {
        val intent = Intent(this, dest).apply {
            if (data != null)
                putExtra(tag, data)

        }
        startActivity(intent)
    }
}

fun navigateForResult(origin: Activity, dest: Class<*>, data: LocalData? = null) {
    with(origin) {
        val intent = Intent(this, dest).apply {
            if (data != null) {
                putExtra(USER_DATA, data)
            }
        }
        startActivityForResult(intent, 1)
    }
}

fun waitingRoomNavigateToGame(origin: Activity, dest: Class<*>, userDta: LocalData? = null, gameId: Int?) {
    with(origin) {
        val intent = Intent(this, dest).apply {
            if (userDta != null)
                putExtra(USER_DATA, userDta)
            if (gameId != null)
                putExtra(WAITINGROOM_GAMEID, gameId.toString())
        }
        startActivity(intent)
    }
}
