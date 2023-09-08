package pdm.battleshipApp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.domain.UserIdAndToken
import pdm.battleshipApp.ui.notification.ErrorDialog
import pdm.battleshipApp.ui.TopBar
import pdm.battleshipApp.utils.*


data class LoginScreenState(
    val userIdAndToken: UserIdAndToken?,
    val responseState: ResponseState?,
    val loadingState: RefreshingState = RefreshingState.Idle,
    val exception: Exception? = null,
)

@Composable
fun LoginScreen(
    state: LoginScreenState,
    onBackRequested: () -> Unit = {},
    login: (String, String) -> Unit = { _, _ -> },
    setLoginValue: (UserIdAndToken, String) -> Unit = {u,s ->}
) {
    var username by remember {
        mutableStateOf("Player2")
    }
    var pass by remember {
        mutableStateOf("PlayerPass@2")
    }
    var textBoxEnable by remember {
        mutableStateOf(true)
    }
    val onChangeUsername: (String) -> Unit = {
        username = it
    }
    val onChangePass: (String) -> Unit = {
        pass = it
    }
    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = MaterialTheme.colors.background,
            topBar = { TopBar("", onBackRequested = { onBackRequested.invoke() }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                Row {
                    Text(text = "Username")
                }
                Row() {
                    SimpleOutlinedTextFieldSample(username, onChangeUsername, textBoxEnable)
                }
                Row() {
                    Text(text = "Password")
                }
                Row() {
                    PasswordTextField(pass, onChangePass, textBoxEnable)
                }
                Row() {
                    Button(onClick = {
                        login(username, pass)
                        textBoxEnable = false
                    }, enabled = stringNotBlank(username, pass)) {
                        if (state.loadingState !== RefreshingState.Idle) {
                            RefreshFab(onClick = {}, state = state.loadingState)
                        }else {
                            Text(text = "LOGIN")
                        }
                        if (state.responseState === ResponseState.Success && !textBoxEnable) {
                            setLoginValue(state.userIdAndToken!!, username)
                        }

                        if (state.responseState === ResponseState.Failure && !textBoxEnable) {
                            ErrorDialog(
                                state.exception!!,
                                networkOnDismiss = {
                                    login(
                                        username,
                                        pass
                                    )
                                },
                                appOnDismiss = {
                                    textBoxEnable = true
                                },
                                serverOnDismiss = {
                                    textBoxEnable = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(LoginScreenState(null, null))
}