package pdm.battleshipApp.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.domain.LocalData
import pdm.battleshipApp.domain.UserIdAndToken
import pdm.battleshipApp.ui.screens.LoginScreen
import pdm.battleshipApp.ui.screens.LoginScreenState
import pdm.battleshipApp.utils.*
import pdm.battleshipApp.viewModel.LoginScreenViewModel

class LoginScreenActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }

    private val viewModel: LoginScreenViewModel by viewModels {
        viewModelInit {
            LoginScreenViewModel(dependencies.service)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent() {
            val userLogin: (String, String) -> Unit = { username, pass ->
                try {
                    viewModel.setLogin(username, pass)
                } catch (_: Exception) {
                }
            }
            val loadingState: RefreshingState =
                if (viewModel.isLoading) RefreshingState.Refreshing
                else RefreshingState.Idle
            val successOrFailOrNull = if (viewModel.userIdAndToken?.isFailure == true) {
                ResponseState.Failure
            } else if (viewModel.userIdAndToken?.isSuccess == true) {
                ResponseState.Success
            } else null

            LoginScreen(
                LoginScreenState(viewModel.userIdAndToken?.getOrNull(), successOrFailOrNull,
                    loadingState,
                    viewModel.userIdAndToken?.exceptionOrNull() as Exception?
                ),
                onBackRequested = { finish() },
                userLogin,
                setLoginValue,
            )

        }
    }

    private val setLoginValue: (userIdAndToken: UserIdAndToken, username: String) -> Unit = {it, username->
        val intent = Intent()
        val data = LocalData(it.userId, it.token, username)
        intent.putExtra(LOGIN_VALUE_RETURN, data)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


}
