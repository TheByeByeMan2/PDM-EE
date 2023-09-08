package pdm.battleshipApp.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun SimpleOutlinedTextFieldSample(text: String, onChangeUsername: (String) -> Unit, enable:Boolean = true) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = text,
        onValueChange = {
            onChangeUsername(it)
        },
        label = { Text("Enter username") },
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        readOnly = !enable,
        modifier = Modifier.background(Color.White)
    )
}

@Composable
fun PasswordTextField(password:String, onChangePass: (String) -> Unit, enable:Boolean = true) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = password,
        onValueChange = {
            onChangePass(it)
        },
        label = { Text("Enter password") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        readOnly = !enable,
        modifier = Modifier.background(Color.White)
    )
}

fun stringNotBlank(s1: String, s2:String) = s1.isNotBlank() && s2.isNotBlank()