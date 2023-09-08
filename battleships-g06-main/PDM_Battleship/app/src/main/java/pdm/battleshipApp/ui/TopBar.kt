package pdm.battleshipApp.ui

import androidx.compose.foundation.background
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pdm.battleship.R

const val PlayerInfoButtonTag = "PlayerInfoButton"
const val BackButtonTag = "BackButton"

@Composable
@Preview
fun TopBar(
    name: String = stringResource(id = R.string.app_name),
    onBackRequested: (() -> Unit)? = null,
    onInfoRequested: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(text = name) },
        contentColor = Color.Transparent,
        backgroundColor = Color.Transparent,

        navigationIcon = {
            if (onBackRequested != null) {
                IconButton(onClick = onBackRequested, modifier = Modifier.testTag(BackButtonTag)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = {
            if (onInfoRequested != null) {
                IconButton(
                    onClick = onInfoRequested, modifier = Modifier
                        .background(Color.White)
                        .testTag(
                            PlayerInfoButtonTag
                        )
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = "Localized description")
                }
            }
        }
    )
}