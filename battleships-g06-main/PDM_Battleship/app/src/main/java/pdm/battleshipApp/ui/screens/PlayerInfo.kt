package pdm.battleshipApp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.UserInfo
import pdm.battleshipApp.activity.AuthorScreenActivity
import pdm.battleshipApp.domain.UserComplexInfoRes
import pdm.battleshipApp.domain.UserDetail
import pdm.battleshipApp.domain.UserIdAndToken
import pdm.battleshipApp.domain.UserState
import pdm.battleshipApp.ui.TopBar
import pdm.battleshipApp.utils.RefreshingState

const val UnauthenticatedPlayerScreenTag = "UnauthenticatedPlayerScreen"
const val AuthenticatedPlayerScreenTag = "AuthenticatedPlayerScreen"

@Composable
fun PlayerScreen(
    onBackRequested: () -> Unit = {},
    goToLogin: () -> Unit = {},
    goToRegister: () -> Unit = {}
) {
    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag(UnauthenticatedPlayerScreenTag),
            backgroundColor = MaterialTheme.colors.background,
            topBar = { TopBar("", onBackRequested = { onBackRequested.invoke() }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                    Row() {
                        Button(onClick = { goToLogin.invoke() }) {
                            Text(text = "LOGIN")
                        }
                    }
                    Row() {
                        Button(onClick = { goToRegister.invoke() }) {
                            Text(text = "REGISTER")
                        }
                    }
            }
        }
    }
}

data class PlayerInfoScreenState(val userDetail: UserDetail?,val loadingState: RefreshingState = RefreshingState.Idle)

@Composable
fun PlayerInfoScreen(
    state: PlayerInfoScreenState,
    onBackRequested: () -> Unit = {},
    logOut: () -> Unit = {},
){
    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag(AuthenticatedPlayerScreenTag),
            backgroundColor = MaterialTheme.colors.background,
            topBar = { TopBar("", onBackRequested = { onBackRequested.invoke() }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                Row() {
                    Text(text = "USER INFO")
                }
                if (state.userDetail != null)
                    UserInfo(state.userDetail, logOut)
                else {
                    Text("...is Loading")
                }
            }
        }
    }
}

@Composable
fun UserInfo(userDetail: UserDetail, logOut: () -> Unit){
    Column {
        Row {
            Text(text = "username = ${userDetail.name}")
        }
        Row {
            Text(text = "game played = ${userDetail.gamesPlayed}")
        }
        Row {
            Text(text = "score = ${userDetail.score}")
        }
        Row {
            Button(onClick = logOut) {
                Text("Log Out")
            }
        }
    }
}

@Composable
fun ComplexInfo(userState: UserState){
    Row {
        Text(text = userState.name)
    }
}