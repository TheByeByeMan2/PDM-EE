package pdm.battleshipApp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.domain.Authors
import pdm.battleshipApp.utils.RefreshFab
import pdm.battleshipApp.utils.RefreshingState

data class AuthorsScreenState(
    val authors: Authors? = null,
    val loadingState: RefreshingState = RefreshingState.Idle
)

const val AuthorScreenTag = "AuthorScreen"

@Composable
fun AuthorScreen(
    state: AuthorsScreenState,
    onBackRequested: () -> Unit = {},
    onSendEmailRequested: () -> Unit = {}
) {
    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag(AuthorScreenTag),
            backgroundColor = MaterialTheme.colors.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                Row {
                    Button(onClick = onBackRequested) {
                        Text(text = "BACK")
                    }
                }
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                    Column() {
                        if (state.authors == null) {
                            RefreshFab(
                                onClick = {},
                                state = state.loadingState
                            )
                        } else {
                            state.authors.authors.forEach {
                                Text(text = "Author: $it")
                            }
                            Text(text = "Version: ${state.authors.version}")
                            Button(onClick = { onSendEmailRequested.invoke() }) {
                                Text(text = "Mail")
                            }
                        }
                    }
                }
            }

        }
    }


}

@Preview
@Composable
fun AuthorsScreenPreview(){
    AuthorScreen(AuthorsScreenState(Authors(listOf("Kaiwei", "Tiago"), 0.1)), {})
}
