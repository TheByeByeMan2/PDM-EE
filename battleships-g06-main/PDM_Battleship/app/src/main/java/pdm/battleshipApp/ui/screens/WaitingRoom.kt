package pdm.battleshipApp.ui.screens

import android.service.autofill.UserData
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.domain.UserIdAndToken
import pdm.battleshipApp.domain.WaitingRoom
import pdm.battleshipApp.domain.WaitingRoomMsg
import pdm.battleshipApp.ui.TopBar
import pdm.battleshipApp.utils.JacksonConv
import pdm.battleshipApp.utils.RefreshFab
import pdm.battleshipApp.utils.RefreshingState
import pdm.battleshipApp.utils.ResponseState

data class WaitingRoomScreenState(
    val waitingRoom: WaitingRoom?,
    val loadingState: RefreshingState = RefreshingState.Idle,
    val resultState: ResponseState?,
    val waitingRoomMsg: WaitingRoomMsg?
)

const val AuthenticatedWaitingRoomScreenTag = "AuthenticatedWaitingRoomScreen"
const val UnauthenticatedWaitingRoomScreenTag = "UnauthenticatedWaitingRoomScreen"

@Composable
fun WaitingRoomScreen(
    state: WaitingRoomScreenState,
    onBackRequested: () -> Unit,
    startGame: (Int) -> Unit = {}
) {
    PDM_BattleshipTheme {
        var gameId by remember {
            mutableStateOf<Int?>(null)
        }
        Scaffold(
            modifier = Modifier.testTag(AuthenticatedWaitingRoomScreenTag),
            topBar = { TopBar("", onBackRequested = onBackRequested) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.loadingState !== RefreshingState.Idle) {
                    RefreshFab(onClick = {}, state = state.loadingState)
                }
                if (state.waitingRoom == null){
                    Text(text = "Wait Waiting Room")
                }else{
                    gameId = state.waitingRoom.gameId ?: gameId
                    if (gameId == null){
                        Text(text = "Waiting Player")
                    }else{
                        startGame(gameId!!)
                    }
                }
                //val msg = if (state.waitingRoomMsg == null) "null" else JacksonConv.classToJson(state.waitingRoomMsg)
                //Text(text = msg)
            }
        }
    }
}

@Composable
fun UnauthenticatedWaitingRoom(
    onBackRequested: () -> Unit,
    goToLogin: () -> Unit
) {
    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier.testTag(UnauthenticatedWaitingRoomScreenTag),
            topBar = { TopBar("", onBackRequested = onBackRequested) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "You not are login, need logged first")
                /*
                Button(onClick = goToLogin ) {
                    Text(text = "Login")
                }
                 */
            }
        }
    }
}

@Preview
@Composable
fun WaitingRoomPreview() {
    WaitingRoomScreen(
        WaitingRoomScreenState(
            WaitingRoom(1, "a", 1, 1),
            RefreshingState.Idle,
            ResponseState.Success,
            null
        ),
        {})
}

@Preview
@Composable
fun UnauthenticatedWaitingRoomPreview() {
    UnauthenticatedWaitingRoom({}, {})
}