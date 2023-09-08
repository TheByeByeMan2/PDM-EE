package pdm.battleshipApp.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import pdm.battleship.R
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.domain.LocalData
import pdm.battleshipApp.ui.notification.ErrorAlert
import pdm.battleshipApp.ui.screens.PlayerInfoScreen
import pdm.battleshipApp.ui.screens.PlayerInfoScreenState
import pdm.battleshipApp.ui.screens.PlayerScreen
import pdm.battleshipApp.utils.*
import pdm.battleshipApp.viewModel.PlayerInfoScreenViewModel
import java.io.IOException

@Suppress("DEPRECATION")
class PlayerInfoActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }
    var userData by mutableStateOf<LocalData?>(null)
    private var isLogOut by mutableStateOf(false)

    private var receiveUserDataFromHome by mutableStateOf(true)

    private val viewModel: PlayerInfoScreenViewModel by viewModels {
        viewModelInit {
            PlayerInfoScreenViewModel(dependencies.service)
        }
    }

    private val logOut: () -> Unit = {
        Log.v("Player Info", "Log Out")
        userData = null
        receiveUserDataFromHome = false
        isLogOut = true
        cleanHomeData()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (receiveUserDataFromHome && getHomeData != null) userData = getHomeData
            if (isLogOut) userData = null

            if (userData != null) {
                Log.v(":::Player Info::", "Login")
                // SIGN IN
                if (userData != null)
                    if (viewModel.userDetail == null) {
                        viewModel.fetchUserInfo(
                            userData!!.userId,
                            userData!!.token
                        )
                    }

                val loadingState: RefreshingState =
                    if (viewModel.isLoading) RefreshingState.Refreshing
                    else RefreshingState.Idle

                PlayerInfoScreen(
                    state = PlayerInfoScreenState(
                        viewModel.userDetail?.getOrNull(),
                        loadingState
                    ),
                    onBackRequested = { sendToHome(userData);receiveUserDataFromHome = true },
                    logOut,
                )

                if (viewModel.userDetail?.isFailure == true)
                    ErrorMessage()

            } else {
                // SIGN OUT
                PlayerScreen(
                    onBackRequested = { finish() },
                    goToLogin = {
                        navigateForResult(
                            this,
                            LoginScreenActivity::class.java
                        )
                    },
                    goToRegister = { navigate(this, RegisterScreenActivity::class.java) })
            }
        }

    }

    private fun sendData(userLocalData: LocalData?) {
        val intent = Intent()
        intent.putExtra(LOGIN_VALUE_RETURN, userLocalData)
        setResult(Activity.RESULT_OK, intent)
    }

    private val sendToHome: (userIdAndToken: LocalData?) -> Unit = {
        sendData(it)
        finish();
    }

    private fun cleanHomeData() {
        sendData(null)
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
                if (returnedData != null) {
                    isLogOut = false
                    userData = returnedData
                }
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


    @Composable
    private fun ErrorMessage() {
        try {
            viewModel.userDetail?.getOrThrow()
        } catch (e: IOException) {
            ErrorAlert(
                title = R.string.error_api_title,
                message = R.string.error_could_not_reach_api,
                buttonText = R.string.error_exit_button_text,
                onDismiss = { finishAndRemoveTask() }
            )
        }
    }

}