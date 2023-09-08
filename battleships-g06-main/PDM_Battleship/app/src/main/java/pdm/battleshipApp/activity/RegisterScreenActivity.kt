package pdm.battleshipApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.ui.screens.RegisterScreen
import pdm.battleshipApp.ui.screens.RegisterScreenState
import pdm.battleshipApp.utils.RefreshingState
import pdm.battleshipApp.utils.ResponseState
import pdm.battleshipApp.utils.viewModelInit
import pdm.battleshipApp.viewModel.RegisterScreenViewModel

class RegisterScreenActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }

    private val viewModel: RegisterScreenViewModel by viewModels {
        viewModelInit {
            RegisterScreenViewModel(dependencies.service)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent() {
            val userRegister: (String, String) -> Unit = { username, pass ->
                try {
                    viewModel.register(username, pass)
                } catch (_: Exception) {
                }
            }
            val loadingState: RefreshingState =
                if (viewModel.isLoading) RefreshingState.Refreshing
                else RefreshingState.Idle

            val successOrFailOrNull = if (viewModel.user?.isFailure == true) {
                ResponseState.Failure
            } else if (viewModel.user?.isSuccess == true) {
                ResponseState.Success
            } else null

            RegisterScreen(
                RegisterScreenState(
                    viewModel.user?.getOrNull(),
                    successOrFailOrNull,
                    loadingState,
                    viewModel.user?.exceptionOrNull() as Exception?
                ),
                onBackRequested = { finish() },
                userRegister,
            )
        }
    }
}