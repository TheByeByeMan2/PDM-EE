package pdm.battleshipApp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import pdm.battleship.R
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.domain.User
import pdm.battleshipApp.exceptions.PasswordsNotEqual
import pdm.battleshipApp.ui.notification.ErrorDialog
import pdm.battleshipApp.ui.SuccessDialog
import pdm.battleshipApp.ui.TopBar
import pdm.battleshipApp.utils.*

data class RegisterScreenState(
    val user: User?,
    val responseState: ResponseState?,
    val loadingState: RefreshingState,
    val exception: Exception?
)

@Composable
fun RegisterScreen(
    state: RegisterScreenState,
    onBackRequested: () -> Unit = {},
    setRegister: (String, String) -> Unit
) {
    var textBoxEnable by remember {
        mutableStateOf(true)
    }
    var username by remember {
        mutableStateOf("Player4")
    }
    var pass by remember {
        mutableStateOf("Player4Pass@")
    }
    var passVer by remember {
        mutableStateOf("Player4Pass@")
    }
    val onChangeUsername: (String) -> Unit = {
        username = it
    }
    val onChangePass: (String) -> Unit = {
        pass = it
    }
    val onChangePassVer: (String) -> Unit = {
        passVer = it
    }
    var passAndVerPass by remember {
        mutableStateOf<Boolean?>(null)
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
                    Text(text = "Confirm Password")
                }
                Row() {
                    PasswordTextField(passVer, onChangePassVer, textBoxEnable)
                }
                Row() {
                    Button(
                        onClick = {
                            passAndVerPass = cmpPass(pass, passVer)
                            if (passAndVerPass == true) {
                                setRegister(username, pass)
                                textBoxEnable = false
                            }
                        },
                        enabled = stringNotBlank(username, pass)
                    ) {
                        if (state.loadingState !== RefreshingState.Idle) {
                            RefreshFab(onClick = {}, state = state.loadingState)
                        } else {
                            Text(text = "REGISTER")
                        }
                        if (state.responseState === ResponseState.Success && !textBoxEnable) {
                            SuccessDialog(
                                title = R.string.success_register_title,
                                message = R.string.success_register_message,
                                buttonText = R.string.confirm_close_button_text,
                                onConfirm = { onBackRequested() }
                            )
                        }
                        if (state.responseState === ResponseState.Failure && !textBoxEnable) {
                            ErrorDialog(
                                e = state.exception!!,
                                networkOnDismiss = { setRegister(username, pass) },
                                serverOnDismiss = { textBoxEnable = true },
                                appOnDismiss = { textBoxEnable = true })
                        }
                        if (passAndVerPass == false) {
                            ErrorDialog(
                                e = PasswordsNotEqual(),
                                appOnDismiss = { textBoxEnable = true; passAndVerPass = null })
                        }
                    }
                }
            }
        }
    }
}

fun cmpPass(pass1: String, pass2: String) =
    pass1 == pass2 && pass1.isNotBlank() && pass2.isNotBlank()