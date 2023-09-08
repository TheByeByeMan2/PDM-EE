package pdm.battleshipApp.ui.notification

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pdm.battleship.R
import pdm.battleshipApp.exceptions.*


@Composable
fun ErrorDialog(
    e: Exception,
    networkOnDismiss: () -> Unit = {},
    appOnDismiss: () -> Unit = {},
    serverOnDismiss: () -> Unit = {}
) {
    when (e) {
        is AppException -> ApiExceptionResult(e, appOnDismiss)
        is ServerException -> ServerExceptionResult(e, serverOnDismiss)
        else -> ErrorAlert(
            title = R.string.error_api_title,
            message = R.string.error_could_not_reach_api,
            buttonText = R.string.error_retry_button_text,
            onDismiss = networkOnDismiss
        )
    }
}

@Composable
fun ServerExceptionResult(e: Exception, onDismiss: () -> Unit = {}) {
    when (e) {
        is ServerUnknownException -> ErrorAlert(
            title = R.string.undefined_title,
            message = R.string.undefined_message,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        is LoginException -> ErrorAlertEditableMessage(
            title = R.string.error_login_title,
            message = e.message!!,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        is RegisterServerException -> ErrorAlertEditableMessage(
            title = R.string.error_register_title,
            message = e.message!!,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        is ServerWaitingRoomException-> ErrorAlertEditableMessage(
            title = R.string.error_waitingRoom_title,
            message = e.message!!,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        is ServerGameException -> ErrorAlertEditableMessage(
            title = R.string.error_Game_title,
            message = e.message!!,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        else -> ErrorAlertEditableMessage(
            title = R.string.undefined_title,
            message = e.message!!,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun ApiExceptionResult(e: Exception, onDismiss: () -> Unit = {}) {
    when (e) {
        is InvalidPositionException -> ErrorAlert(
            title = R.string.invalid_position_title,
            message = R.string.invalid_position_message,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        is InvalidPutException -> ErrorAlert(
            title = R.string.undefined_title,
            message = R.string.undefined_message,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        is PasswordsNotEqual -> ErrorAlert(
            title = R.string.error_register_title,
            message = R.string.error_register_passNotEqual_message,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
        else -> ErrorAlert(
            title = R.string.undefined_title,
            message = R.string.undefined_message,
            buttonText = R.string.error_close_button_text,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ErrorAlertEditableMessage(
    @StringRes title: Int,
    message: String,
    @StringRes buttonText: Int,
    onDismiss: () -> Unit = { }
) {
    ErrorAlertImpl(
        title = stringResource(id = title),
        message = message,
        buttonText = stringResource(id = buttonText),
        onDismiss = onDismiss
    )
}

@Composable
fun ErrorAlert(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes buttonText: Int,
    onDismiss: () -> Unit = { }
) {
    ErrorAlertImpl(
        title = stringResource(id = title),
        message = stringResource(id = message),
        buttonText = stringResource(id = buttonText),
        onDismiss = onDismiss
    )
}

@Composable
private fun ErrorAlertImpl(
    title: String,
    message: String,
    buttonText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        buttons = {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                OutlinedButton(
                    border = BorderStroke(0.dp, Color.Unspecified),
                    onClick = onDismiss
                ) {
                    Text(text = buttonText)
                }
            }
        },
        title = { Text(text = title) },
        text = { Text(text = message) },
        modifier = Modifier.testTag("ErrorAlert")
    )
}