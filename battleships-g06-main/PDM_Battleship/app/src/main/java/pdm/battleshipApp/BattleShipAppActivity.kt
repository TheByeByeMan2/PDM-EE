package pdm.battleshipApp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import pdm.battleshipApp.activity.*
import pdm.battleshipApp.domain.LocalData
import pdm.battleshipApp.domain.UserIdAndToken
import pdm.battleshipApp.ui.screens.*
import pdm.battleshipApp.utils.*

@Suppress("DEPRECATION")
class BattleShipAppActivity : ComponentActivity() {

    private var userData by mutableStateOf<LocalData?>(null)
    private var recntFlag by mutableStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (getHomeData != null) {
                userData = getHomeData
            }
            HomeScreen(
                goToRanking = { navigate(this, RankingScreenActivity::class.java) },
                goToAuthor = { navigate(this, AuthorScreenActivity::class.java) },
                goToStart = {
                    navigate(
                        this,
                        WaitingRoomScreenActivity::class.java,
                        userData
                    )
                },
                goToPlayerInfo = {
                    navigateForResult(
                        this,
                        PlayerInfoActivity::class.java,
                        userData
                    )
                },
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (resultCode == Activity.RESULT_OK) {
                val returnedData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    data!!.getParcelableExtra(LOGIN_VALUE_RETURN, LocalData::class.java)
                else
                    data!!.getParcelableExtra(LOGIN_VALUE_RETURN)
                userData = returnedData
            }
        }
    }

    @Suppress("deprecation")
    private val getHomeData: LocalData?
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(USER_DATA, LocalData::class.java)
            } else {
                intent.getParcelableExtra(USER_DATA)
            }
}
