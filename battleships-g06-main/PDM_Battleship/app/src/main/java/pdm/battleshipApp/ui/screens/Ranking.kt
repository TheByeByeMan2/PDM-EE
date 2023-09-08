package pdm.battleshipApp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.internal.wait
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.domain.UserDetail
import pdm.battleshipApp.ui.TopBar
import pdm.battleshipApp.utils.RefreshFab
import pdm.battleshipApp.utils.RefreshingState
import pdm.battleshipApp.viewModel.RankingScreenViewModel

val RankingSize = 40.sp

const val RankingScreenTag = "RankingScreen"

data class RankingScreenState(
    val users: List<UserDetail>? = null,
    val loadingState: RefreshingState = RefreshingState.Idle
)

@Composable
fun RankingScreen(
    state: RankingScreenState,
    onBackRequested: () -> Unit = {},
    onUpdateRequest: (() -> Unit)? = null
) {
    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag(RankingScreenTag),
            backgroundColor = MaterialTheme.colors.background,
            floatingActionButton = {
                if (onUpdateRequest != null) {
                    RefreshFab(
                        onClick = onUpdateRequest,
                        state = state.loadingState
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            topBar = {
                TopBar("", onBackRequested = onBackRequested)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                Row() {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "RANKING", fontSize = RankingSize)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    if (state.users != null)
                        ScrollBoxes(state.users)
                }
            }
        }

    }

}

@Composable
fun ScrollBoxes(users: List<UserDetail>) {
    Row(
        modifier = Modifier
            .background(Color.LightGray)
            .width(600.dp)
            .height(250.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top,
    ) {
        Column() {
            Row() {
                Text("Player name")
            }
            users.forEach {
                Row() {
                    Text(it.name)
                }
            }
        }
        Column() {
            Row() {
                Text("Player game played")
            }
            users.forEach {
                Row() {
                    Text(it.gamesPlayed.toString())
                }
            }
        }
        Column() {
            Row() {
                Text("Player score")
            }
            users.forEach {
                Row() {
                    Text(it.score.toString())
                }
            }
        }

    }
}

@Preview
@Composable
fun RankingScreenPreview() {
    val list =
        listOf<UserDetail>(UserDetail(1, "player1", 100, 50), UserDetail(2, "player2", 90, 50))
    val state = RankingScreenState(list)
    RankingScreen(state)
}