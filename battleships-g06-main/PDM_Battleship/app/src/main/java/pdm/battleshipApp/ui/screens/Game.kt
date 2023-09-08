package pdm.battleshipApp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.delay
import pdm.battleship.R
import pdm.battleship.ui.theme.PDM_BattleshipTheme
import pdm.battleshipApp.battleShip.model.*
import pdm.battleshipApp.battleShip.model.MyBoard
import pdm.battleshipApp.battleShip.model.coords.COLUMN_DIM
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.coords.ROW_DIM
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.domain.GameState
import pdm.battleshipApp.gameModel.Player
import pdm.battleshipApp.ui.DirectionSelector
import pdm.battleshipApp.ui.ShipSelector
import pdm.battleshipApp.ui.SuccessDialog
import pdm.battleshipApp.ui.TopBar
import pdm.battleshipApp.utils.RefreshFab
import pdm.battleshipApp.utils.RefreshingState
import pdm.battleshipApp.utils.colCalculator
import pdm.battleshipApp.utils.rowCounter
import java.sql.Timestamp


val CELL_DIM = 10.dp
val SHIP_CELL_WIDTH = 5.dp
val SHIP_CELL_HEIGHT = 20.dp
val COORDS_CELL_DIM = 20.dp
val WATER_COLOR = Color.Cyan
val SHIP_COLOR = Color.Blue
val MISS_COLOR = Color.LightGray
val SHOT_COLOR = Color.Red
val SINK_COLOR = Color.DarkGray

data class GameScreenState(
    val userId: Int,
    val username: String,
    val game: Game?,
    val currentShipType: ShipType,
    val currentDirection: Direction,
    val finalGame: Game?,
    val loadingState: RefreshingState
)

@Composable
fun StartScreen(
    state: GameScreenState,
    onBackRequested: () -> Unit = {},
    onClickSelectShipType: (ShipType) -> Unit = {},
    onClickSelectDirection: (Direction) -> Unit = {},
    myBoardOnClick: (Position) -> Unit = {},
    gameReady: () -> Unit = {},
    shotOtherGrid: (Position) -> Unit = {},
    timerText: String,
    turnText: String,
    waitingOtherPlayer: () -> Unit = {},
    getFinalGame: () -> Unit = {},
    startTime:()->Unit = {},
    endGame:()->Unit = {},
    backHome:()->Unit = {},
    timeover: ()->Unit = {},
    getInfo: ()->Unit = {}
) {

    var startWaiting by remember {
        mutableStateOf(false)
    }
    var oneTime by remember {
        mutableStateOf(true)
    }

    var isMyTurn by remember {
        mutableStateOf(false)
    }

    var prevInitialTurn by remember {
        mutableStateOf<Timestamp?>(null)
    }

    var time by remember {mutableStateOf(false)}

    var gameover by remember {mutableStateOf(false)}

    var timeAlert by remember {mutableStateOf(false)}

    PDM_BattleshipTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            backgroundColor = MaterialTheme.colors.background,
            topBar = { TopBar("", onBackRequested = { onBackRequested.invoke() }) }
        ) { innerPadding ->
            Column {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text(text = timerText)
                }
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text(text = turnText)
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth(0.5F)
                            .fillMaxHeight(0.9F)
                    ) {
                        if (state.game != null) {
                            val disable = state.game.gameState !== GameState.START
                            MyVisualBoard(state.game.boards.myBoard, myBoardOnClick, disable)
                        } else {
                            RefreshFab(onClick = {}, RefreshingState.Refreshing)
                        }

                        if (!startWaiting && state.game?.gameState === GameState.START) {
                            Button(onClick = {
                                try {
                                    gameReady()
                                    startWaiting = true
                                }catch (e: Exception){
                                    startWaiting = false
                                }
                            }) {
                                Text(text = "READY")
                            }
                        } else {
                            RefreshFab(onClick = {}, state.loadingState)
                        }
                    }
                    Column(modifier = Modifier.width(20.dp)) {}
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        if (state.game?.boards?.myBoard?.getRemainShip != null) {
                            if (state.game.initialTurn != prevInitialTurn){
                                startTime()
                                prevInitialTurn = state.game.initialTurn
                            }
                            Column() {
                                isMyTurn = state.game.turn == Player(state.userId)
                                if (!isMyTurn && state.game.gameState === GameState.BATTLE && oneTime) {
                                    waitingOtherPlayer()
                                    oneTime = false
                                }
                                if (isMyTurn) {
                                    oneTime = true
                                }
                                when (state.game.gameState) {

                                    GameState.START -> Selector(
                                        onClickSelectShipType,
                                        state.currentShipType,
                                        onClickSelectDirection,
                                        state.currentDirection,
                                        state.game.boards.myBoard.getRemainShip
                                    )

                                    GameState.BATTLE -> OtherBoard(
                                        state.game.boards.other,
                                        !isMyTurn,
                                        shotOtherGrid
                                    )

                                    GameState.END -> {
                                        getFinalGame()
                                        if (state.finalGame == null) {
                                            RefreshFab(onClick = {})
                                        } else {
                                            if (state.finalGame.winner == state.username) {
                                                SuccessDialog(
                                                    R.string.win,
                                                    R.string.win_message,
                                                    R.string.confirm_close_button_text,
                                                    { endGame(); backHome()}
                                                )
                                            } else {
                                                SuccessDialog(
                                                    R.string.lose,
                                                    R.string.lose_message,
                                                    R.string.confirm_close_button_text,
                                                    { endGame();backHome() }
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        } else {
                            RefreshFab(onClick = {}, RefreshingState.Refreshing)
                        }
                    }
                }
                if (isMyTurn && checkTimer(timerText) && !timeAlert) time = true
                if (time && !timeAlert) {
                    timeAlert = true
                    AlertDialog(
                        onDismissRequest = { time = false },
                        title = { Text(text = "POUCO TEMPO PARA JOGAR!", color = Color.Red) },
                        buttons = {}
                    )
                }
                if (isMyTurn && timerText == "1" && !gameover) {
                    gameover = true
                    timeover()
                }
                if (!isMyTurn) getInfo()
                }
            }
        }
    }

@Composable
fun OtherBoard(otherBoard: EnigmaBoard, onclickDisable: Boolean, shot: (Position) -> Unit) {
    val newColSize = COLUMN_DIM + 1
    val newRowSize = ROW_DIM + 1
    var count = 1
    LazyVerticalGrid(
        columns = GridCells.Fixed(newColSize),
    ) {
        val total = (newColSize) * (newRowSize)
        items(total) {
            //col in 1 .. col dim
            val col = colCalculator(count, newColSize)
            //row in 1 .. col dim
            val row = rowCounter(count, newRowSize)
            EnigmaCellView(col, row, otherBoard, shot, onclickDisable)
            count += 1
        }
    }
}

@Composable
fun EnigmaCellView(
    col: Int,
    row: Int,
    otherBoard: EnigmaBoard,
    onclick: (Position) -> Unit,
    onclickDisable: Boolean
) {
    // boardCol in 0 .. col dim -1
    val boardCol = col - 1
    // boardRow in 0 .. row dim -1
    val boardRow = row - 1
    // boardCol and boardRow are used to coordinate cell
    if (boardRow == 0 || boardCol == 0) {
        if (boardRow != 0 || boardCol != 0) {
            if (boardRow == 0) {
                Coords(pdm.battleshipApp.battleShip.model.coords.Column.values()[boardCol - 1].name)
            }
            if (boardCol == 0) {
                Coords(pdm.battleshipApp.battleShip.model.coords.Row.values()[boardRow - 1].number.toString())
            }
        }
    } else {
        // here the boardCol and boardRow is between 1 .. col or row dim
        val colIndex = boardCol - 1
        val rowIndex = boardRow - 1
        val gridCellState = otherBoard.grid.get(Position[colIndex, rowIndex])
        ShipCell(colIndex, rowIndex, gridCellState, onclick, onclickDisable)
    }
}

@Composable
fun MyVisualBoard(myBoard: MyBoard, onClick: (Position) -> Unit, onclickDisable: Boolean) {
    val newColSize = COLUMN_DIM + 1
    val newRowSize = ROW_DIM + 1
    var count = 1
    LazyVerticalGrid(
        columns = GridCells.Fixed(newColSize),
    ) {
        val total = (newColSize) * (newRowSize)
        items(total) {
            //col in 1 .. col dim
            val col = colCalculator(count, newColSize)
            //row in 1 .. col dim
            val row = rowCounter(count, newRowSize)
            CellView(col, row, myBoard, onClick, onclickDisable)
            count += 1
        }
    }
}

@Composable
fun CellView(
    col: Int,
    row: Int,
    myBoard: MyBoard,
    onclick: (Position) -> Unit,
    onclickDisable: Boolean
) {
    // boardCol in 0 .. col dim -1
    val boardCol = col - 1
    // boardRow in 0 .. row dim -1
    val boardRow = row - 1
    // boardCol and boardRow are used to coordinate cell
    if (boardRow == 0 || boardCol == 0) {
        if (boardRow != 0 || boardCol != 0) {
            if (boardRow == 0) {
                Coords(pdm.battleshipApp.battleShip.model.coords.Column.values()[boardCol - 1].name)
            }
            if (boardCol == 0) {
                Coords(pdm.battleshipApp.battleShip.model.coords.Row.values()[boardRow - 1].number.toString())
            }
        }
    } else {
        // here the boardCol and boardRow is between 1 .. col or row dim
        val colIndex = boardCol - 1
        val rowIndex = boardRow - 1
        val gridCellState = myBoard.shipStateToGridCellState(Position[colIndex, rowIndex])
        ShipCell(colIndex, rowIndex, gridCellState, onclick, onclickDisable)
    }
}

@Composable
fun Coords(s: String) {
    val modifier = Modifier
        .size(COORDS_CELL_DIM)
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(text = s)
    }
}


@Composable
fun ShipCell(
    colIndex: Int,
    rowIndex: Int,
    state: GridCellState?,
    onclick: (Position) -> Unit,
    onclickDisable: Boolean
) {

    val pos = Position[colIndex, rowIndex]

    val color = when (state) {
        GridCellState.SHIP -> SHIP_COLOR
        GridCellState.MISS -> MISS_COLOR
        GridCellState.SHOT -> SHOT_COLOR
        GridCellState.SINK -> SINK_COLOR
        else -> WATER_COLOR
    }

    val modifier = Modifier
        .height(SHIP_CELL_HEIGHT)
        .width(SHIP_CELL_WIDTH)
        .background(color)
        .border(1.dp, color = Color.White)
        .clickable(enabled = !onclickDisable, onClick = {
            onclick(pos)
        })
    Box(modifier = modifier) {
    }
}


@Composable
fun Selector(
    onClickSelectShipType: (ShipType) -> Unit,
    currentShipType: ShipType,
    onClickSelectDirection: (Direction) -> Unit,
    currentDirection: Direction,
    remainShip: Map<ShipType, Int>
) {
    Column() {
        ShipSelector(onClickSelectShipType, currentShipType, remainShip)
        DirectionSelector(onClickSelectDirection, currentDirection)
    }
}

@Preview
@Composable
fun GamePreview() {
    StartScreen(
        state = GameScreenState(
            1,
            "Player1",
            Game(
                1,
                Player(1),
                Player(2),
                Boards(),
                null,
                null,
                1,
                GameState.BATTLE,
                Player(2)
            ),
            ShipType.values.first(),
            Direction.HORIZONTAL,
            null,
            RefreshingState.Refreshing
        ), timerText = "100", turnText = "Ola"
    )
}

