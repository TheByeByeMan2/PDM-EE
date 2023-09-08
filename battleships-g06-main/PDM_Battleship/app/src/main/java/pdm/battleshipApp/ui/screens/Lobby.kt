package pdm.battleshipApp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleship.R
import pdm.battleshipApp.ui.TopBar

enum class AppScreenState { HOME, START, LOGIN, RANKING, AUTHOR }

val HomeButtonSize = Modifier
    .height(55.dp)
    .width(250.dp)

val HomeButtonTextSize = 25.sp
val BattleShipTitleSize = 50.sp

const val HomeScreenTag = "HomeScreen"
const val StartButtonTag = "StartButton"
const val AuthorButtonTag = "AuthorButton"
const val RankingButtonTag = "RankingButton"

@Composable
fun HomeScreen(
    goToRanking: () -> Unit = {},
    goToAuthor: () -> Unit = {},
    goToStart: () -> Unit = {},
    goToPlayerInfo: () -> Unit = {},
    temp:()->Unit = {}
    //appState: AppState
) {
    PDM_BattleshipTheme {
        Surface(
            color = Color.Transparent,
            contentColor = Color.Transparent,
            modifier = Modifier.testTag(HomeScreenTag)
            //topBar = { TopBar()},
        ) { //innerPadding ->
            Image(
                painter = painterResource(id = R.drawable.banner3),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )
            TopBar(name = "", onInfoRequested = goToPlayerInfo)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                //.padding(innerPadding),
                , horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    HomeScreenButtons(
                        goToRanking,
                        goToAuthor,
                        goToStart
                        //appState
                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen({}, {}, {}, {})
}

@Composable
fun HomeScreenButtons(
    goToRanking: () -> Unit = {},
    goToAuthor: () -> Unit = {},
    goToStart: () -> Unit = {},
    //appState: AppState
) {
    Row(modifier = Modifier.padding(all = 4.dp), verticalAlignment = Alignment.Bottom) {
        Button(
            onClick = { goToStart.invoke() },
            modifier = HomeButtonSize.testTag(StartButtonTag)
        ) {
            Text(text = AppScreenState.START.name, fontSize = HomeButtonTextSize)
        }
    }
    Row(modifier = Modifier.padding(all = 4.dp), verticalAlignment = Alignment.Bottom) {
        Button(onClick = goToAuthor, modifier = HomeButtonSize.testTag(AuthorButtonTag)) {
            Text(text = AppScreenState.AUTHOR.name, fontSize = HomeButtonTextSize)
        }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = { goToRanking.invoke() }, modifier = HomeButtonSize.testTag(
                RankingButtonTag
            )
        ) {
            Text(text = AppScreenState.RANKING.name, fontSize = HomeButtonTextSize)
        }
    }
}
