package pdm.battleshipApp.ui

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

@Composable
fun SuccessDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes buttonText: Int,
    onConfirm: () -> Unit = { }
) {
    SuccessDialogImpl(
        title = stringResource(id = title),
        message = stringResource(id = message),
        buttonText = stringResource(id = buttonText),
        onConfirm = onConfirm
    )
}

@Composable
private fun SuccessDialogImpl(
    title: String,
    message: String,
    buttonText: String,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        buttons = {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
            ) {
                OutlinedButton(
                    border = BorderStroke(0.dp, Color.Unspecified),
                    onClick = onConfirm
                ) {
                    Text(text = buttonText)
                }
            }
        },
        title = { Text(text = title) },
        text = { Text(text = message) },
        modifier = Modifier.testTag("SuccessDialog")
    )
}