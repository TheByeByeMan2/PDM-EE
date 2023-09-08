package pdm.battleshipApp.activity

import kotlin.concurrent.timer
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import okhttp3.internal.wait
import pdm.battleship.R
import pdm.battleshipApp.BattleShipAppActivity
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.battleShip.model.GridCellState
import pdm.battleshipApp.battleShip.model.MyBoard
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.Ship
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.domain.LocalData
import pdm.battleshipApp.domain.ShipPosDirection
import pdm.battleshipApp.domain.ShotResult
import pdm.battleshipApp.domain.ShotState
import pdm.battleshipApp.exceptions.AppException
import pdm.battleshipApp.exceptions.AppGameException
import pdm.battleshipApp.exceptions.PutAllShipException
import pdm.battleshipApp.gameModel.Player
import pdm.battleshipApp.ui.notification.ErrorAlert
import pdm.battleshipApp.ui.notification.ErrorDialog
import pdm.battleshipApp.ui.screens.GameScreenState
import pdm.battleshipApp.ui.screens.StartScreen
import pdm.battleshipApp.utils.*
import pdm.battleshipApp.viewModel.GameScreenViewModel
import java.io.IOException
import java.sql.Timestamp

class GameScreenActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }
    private var userData by mutableStateOf<LocalData?>(null)
    private var gameId by mutableStateOf<Int>(0)
    private var firstInitialize by mutableStateOf(true)

    private var prevShot by mutableStateOf<ShotResult?>(null)

    private var prevInitialTurn by mutableStateOf<Timestamp?>(null)

    private var error by mutableStateOf<Exception?>(null)

    private val viewModel: GameScreenViewModel by viewModels {
        viewModelInit {
            GameScreenViewModel(dependencies.service)
        }
    }

    private var enabledErrorDialog by mutableStateOf(true)

    val gameReady: () -> Unit = {
        println("::: GAME READY :::")
        try {
            //checkTheShipAreAllPut(viewModel.localShips)
            viewModel.putShipsToServer(userData!!.userId, userData!!.token, gameId)
            viewModel.setReady(userData!!.userId, userData!!.token, gameId)
            viewModel.getGameInfo(userData!!.userId, userData!!.token, gameId)
            viewModel.readyWaiting(userData!!.userId, userData!!.token, gameId, 1000)
        } catch (e: Exception) {
            error = e
        }
    }

    val shotOtherGrid: (Position) -> Unit = {
        println("::: SHOT OTHER GRID :::")
        try {
            viewModel.shotOther(userData!!.userId, userData!!.token, gameId, it)
            viewModel.getGameInfoInBattle(userData!!.userId, userData!!.token, gameId)
        } catch (e: Exception) {
            error = e
        }
    }

    val waitingOtherPlayer: () -> Unit = {
        println("::: WAITING OTHER :::")
        try {
            viewModel.waitingOther(userData!!.userId, userData!!.token, gameId, 1000)
        } catch (e: Exception) {
            error = e
        }
    }

    val startTime: () -> Unit = {
        try {
            viewModel.timer.start()
        } catch (e: Exception) {
            error = e
        }
    }

    val timeover: () -> Unit = {
        try {
            print("início activity timeover")
            viewModel.timeover(userData!!.userId, userData!!.token, gameId)
            print("mid activity timeover")
            viewModel.getGameInfo(userData!!.userId, userData!!.token, gameId)
            print("fim activity timeover")
        } catch (e: Exception) {
            error = e
        }
    }

    val getInfo: () -> Unit = {
        viewModel.getGameInfoPolling(userData!!.userId, userData!!.token, gameId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (gameId == 0) gameId = getGameId!!
            if (userData == null) userData = getHomeData
            require(gameId > 0 && userData?.userId != null && userData?.token != null)

            enabledErrorDialog = true
            error = null

            if (viewModel.shotResult != null && viewModel.shotResult?.getOrNull() != prevShot) {
                println("::: MANAGER SHOT :::")
                viewModel.managerShotResult()
                prevShot = viewModel.shotResult?.getOrNull()
            }
            if (viewModel.timerText.isNotBlank() && viewModel.timerText.toInt() <= 0) {
                viewModel.getGameInfo(userData!!.userId, userData!!.token, gameId)
            }

            if (viewModel.localGame == null && firstInitialize) {
                println("::: INITIALIZE GAME :::")
                viewModel.initializeGame(userData!!.userId, userData!!.token, gameId)
                firstInitialize = false
            }

            val getFinalGame: () -> Unit = {
                try {
                    viewModel.getFinal(userData!!.userId, userData!!.token, gameId)
                } catch (e: Exception) {
                    error = e
                }
            }

            val backHome: () -> Unit = {
                navigate(this, BattleShipAppActivity::class.java, userData)
            }

            val endGame: () -> Unit = {
                try {
                    viewModel.endGame(userData!!.userId, userData!!.token, gameId)
                } catch (e: Exception) {
                    error = e
                }
            }

            val loadingState =
                if (viewModel.isLoading) RefreshingState.Refreshing else RefreshingState.Idle

            StartScreen(
                GameScreenState(
                    userData!!.userId,
                    userData!!.username,
                    viewModel.localGame,
                    viewModel.currentShipType,
                    viewModel.currentDirection,
                    viewModel.finalGetGame?.getOrNull(),
                    loadingState
                ),
                onBackRequested = {
                    viewModel.exitGame(
                        userData!!.userId,
                        userData!!.token,
                        gameId
                    );finish()
                },
                viewModel.onClickSelectShipType,
                viewModel.onClickSelectDirection,
                viewModel.myBoardOnClick,
                gameReady,
                shotOtherGrid,
                viewModel.timerText,
                if (viewModel.localGame?.turn == Player(userData!!.userId)) "Is You are turn" else "Waiting",
                waitingOtherPlayer,
                getFinalGame,
                startTime,
                backHome,
                endGame,
                timeover,
                getInfo
            )

            if (error != null) {
                ErrorMessage(error!!)
            }

            if (viewModel.error != null) {
                ErrorMessage(e = viewModel.error!!)
            }
            /*
            if (viewModel.localGameRes?.isFailure == true){
                LocalGameResErrorMessage()
            }
            if (viewModel.shotResult?.isFailure == true){
                ShotResErrorMessage()
            }
            if (viewModel.finalGetGame?.isFailure == true){
                FinalGameGetErrorMessage()
            }
            if(viewModel.setReadyRes?.isFailure == true){
                SetGameResErrorMessage()
            }

             */

        }
    }

    @Suppress("deprecation")
    private val getHomeData: LocalData?
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(USER_DATA, LocalData::class.java)
            } else {
                intent.getParcelableExtra(USER_DATA)
            }

    private val getGameId: Int?
        get() = intent.getStringExtra(WAITINGROOM_GAMEID)?.toInt()

    @Composable
    private fun ErrorMessage(e: Exception) {
        if (enabledErrorDialog) {
            println(e.message)
            ErrorDialog(e,
                networkOnDismiss = {
                    viewModel.setReady(
                        userData!!.userId,
                        userData!!.token,
                        gameId
                    )
                },
                serverOnDismiss = { enabledErrorDialog = false },
                appOnDismiss = { enabledErrorDialog = false }
            )
        }
    }

    @Composable
    private fun LocalGameResErrorMessage() {
        try {
            viewModel.localGameRes?.getOrThrow()
        } catch (e: IOException) {
            ErrorAlert(
                title = R.string.error_api_title,
                message = R.string.error_could_not_reach_api,
                buttonText = R.string.error_exit_button_text,
                onDismiss = { finishAndRemoveTask() }
            )
        }
    }

    @Composable
    private fun FinalGameGetErrorMessage() {
        try {
            viewModel.finalGetGame?.getOrThrow()
        } catch (e: IOException) {
            ErrorAlert(
                title = R.string.error_api_title,
                message = R.string.error_could_not_reach_api,
                buttonText = R.string.error_exit_button_text,
                onDismiss = { finishAndRemoveTask() }
            )
        }
    }

    @Composable
    private fun SetGameResErrorMessage() {
        try {
            viewModel.setReadyRes?.getOrThrow()
        } catch (e: IOException) {
            ErrorAlert(
                title = R.string.error_api_title,
                message = R.string.error_could_not_reach_api,
                buttonText = R.string.error_exit_button_text,
                onDismiss = { finishAndRemoveTask() }
            )
        }
    }

    @Composable
    private fun ShotResErrorMessage() {
        try {
            viewModel.shotResult?.getOrThrow()
        } catch (e: IOException) {
            ErrorAlert(
                title = R.string.error_api_title,
                message = R.string.error_could_not_reach_api,
                buttonText = R.string.error_exit_button_text,
                onDismiss = { finishAndRemoveTask() }
            )
        }
    }
}

fun checkTheShipAreAllPut(localShip: List<ShipPosDirection>) {
    var total = 0
    ShipType.values.forEach {
        total += it.fleetQuantity
    }
    if (localShip.size < total) throw PutAllShipException()
}

/**
 * waiting
 * shot
 */