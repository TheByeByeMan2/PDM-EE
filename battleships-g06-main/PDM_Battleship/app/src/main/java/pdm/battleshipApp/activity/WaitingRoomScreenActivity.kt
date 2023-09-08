package pdm.battleshipApp.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.domain.LocalData
import pdm.battleshipApp.domain.WaitingRoomMsg
import pdm.battleshipApp.service.RULE_ID
import pdm.battleshipApp.ui.notification.ErrorDialog
import pdm.battleshipApp.ui.screens.UnauthenticatedWaitingRoom
import pdm.battleshipApp.ui.screens.WaitingRoomScreen
import pdm.battleshipApp.ui.screens.WaitingRoomScreenState
import pdm.battleshipApp.utils.*
import pdm.battleshipApp.viewModel.WaitingRoomScreenViewModel

class WaitingRoomScreenActivity : ComponentActivity() {
    private val dependencies by lazy { application as DependenciesContainer }

    private val viewModel: WaitingRoomScreenViewModel by viewModels {
        viewModelInit {
            WaitingRoomScreenViewModel(dependencies.service)
        }
    }

    private var startMatchMaking by mutableStateOf(false)
    private var startSearchPlayer by mutableStateOf(false)
    private var reconnectFlag by mutableStateOf(false)

    var userData by mutableStateOf<LocalData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            userData = getHomeData
            if (userData == null) {
                UnauthenticatedWaitingRoom(
                    { finish() },
                    {
                        navigate(this, LoginScreenActivity::class.java)
                    }
                )
            } else {

                if (!startMatchMaking) {
                    viewModel.matchMaking(userData!!.userId, userData!!.token)
                    viewModel.waitingRoomSender(WaitingRoomMsg(userData!!.userId, RULE_ID))
                    startMatchMaking = true
                }
                if (!startSearchPlayer) {
                    viewModel.getWaitingRoom(userData!!.userId, userData!!.token, 1000)
                    viewModel.waitingRoomListener(userData!!.userId)
                    startSearchPlayer = true
                }
                val loadingState: RefreshingState =
                    if (viewModel.isLoading) RefreshingState.Refreshing
                    else RefreshingState.Idle
                val successOrFailOrNull = if (viewModel.waitingRoom?.isFailure == true) {
                    ResponseState.Failure
                } else if (viewModel.waitingRoom?.isSuccess == true) {
                    ResponseState.Success
                } else null
                val startGame: (Int) -> Unit = {
                    waitingRoomNavigateToGame(
                        this,
                        GameScreenActivity::class.java,
                        LocalData(userData!!.userId, userData!!.token, userData!!.username),
                        it,
                    )
                }
                WaitingRoomScreen(
                    WaitingRoomScreenState(
                        viewModel.waitingRoom?.getOrNull(),
                        loadingState,
                        successOrFailOrNull,
                        viewModel.waitingRoomMsg
                    ),
                    onBackRequested = {
                        viewModel.exitWaitingRoom(
                            userData!!.userId,
                            userData!!.token
                        );finish()
                    },
                    startGame = startGame
                )
                if (viewModel.waitingRoom?.isFailure == true)
                    ErrorMessage()

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
            viewModel.waitingRoom?.getOrThrow()
        } catch (e: Exception) {
            ErrorDialog(e,
                networkOnDismiss = { finishAndRemoveTask() },
                serverOnDismiss = { finishAndRemoveTask() },
                appOnDismiss = { finishAndRemoveTask() }
            )
        }
    }
}