package pdm.battleshipApp.viewModel

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pdm.battleshipApp.battleShip.model.Boards
import pdm.battleshipApp.battleShip.model.Game
import pdm.battleshipApp.battleShip.model.GridCellState
import pdm.battleshipApp.battleShip.model.coords.Direction
import pdm.battleshipApp.battleShip.model.coords.Position
import pdm.battleshipApp.battleShip.model.ship.ShipType
import pdm.battleshipApp.domain.*
import pdm.battleshipApp.gameModel.Player
import pdm.battleshipApp.service.Service
import pdm.battleshipApp.utils.copyNonBoard
import pdm.battleshipApp.utils.toGameAndSaveBoards

class GameScreenViewModel(private val service: Service) : ViewModel() {

    private var _exitGame by
    mutableStateOf<Result<ServerGame?>?>(null)

    val localGame: Game?
        get() = _localGame?.getOrNull()

    val localGameRes: Result<Game>?
        get() = _localGame

    private var _localGame by mutableStateOf<Result<Game>?>(null)

    private var _isLoading by
    mutableStateOf(false)

    var currentShipType by mutableStateOf(ShipType.values.first())
    val onClickSelectShipType: (ShipType) -> Unit = {
        currentShipType = it
    }
    var currentDirection by mutableStateOf(Direction.values().first())
    val onClickSelectDirection: (Direction) -> Unit = {
        currentDirection = it
    }

    var error by mutableStateOf<Exception?>(null)

    var localShips by mutableStateOf<List<ShipPosDirection>>(listOf())

    val myBoardOnClick: (Position) -> Unit = { pos ->
        try {
            error = null
            val game = _localGame?.getOrNull()!!
            _localGame = if (game.boards.myBoard.getShipInFleet(pos) != null) {
                localShips = localShips.filter { it.position != pos }
                Result.success(game.removeFleetMyBoard(pos))
            } else {
                val newl = localShips.toMutableList()
                newl.add(ShipPosDirection(currentShipType, pos, currentDirection))
                localShips = newl.toList()

                Result.success(
                    game.putFleetMyBoard(pos, currentShipType, currentDirection)
                )
            }
        } catch (e: Exception) {
            error = e
        }

    }

    val isLoading: Boolean
        get() = _isLoading

    val exitGame: Result<ServerGame?>?
        get() = _exitGame

    var readyWaitingExit by mutableStateOf(false)
    var waitingOtherExit by mutableStateOf(false)

    val timerText: String get() = countText

    //val deltaTime = System.currentTimeMillis() - localGame!!.initialTurn!!.nanos / 1000
    private var currentTime by mutableStateOf(120000L - 0)
    private var countText by mutableStateOf("")

    val finishPutShips: Boolean get() = _finishPutShips
    private var _finishPutShips by mutableStateOf(false)

    val finishShotShips: Boolean get() = _finishShotShips
    private var _finishShotShips by mutableStateOf(false)

    val shotResult: Result<ShotResult>? get() = _shotResult
    private var _shotResult by mutableStateOf<Result<ShotResult>?>(null)

    private var shotPosition by mutableStateOf<Position?>(null)

    val finalGetGame: Result<Game>? get() = _finalGetGame
    private var _finalGetGame by mutableStateOf<Result<Game>?>(null)

    var setReadyRes: Result<Game>? by mutableStateOf(null)

    val timer = object : CountDownTimer(currentTime, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            currentTime = millisUntilFinished
            updateTimer()
        }

        override fun onFinish() {

        }
    }

    fun updateTimer() {
        val minute = currentTime / 1000
        countText = minute.toString()
    }


    fun putShipsToServer(
        userId: Int,
        token: String,
        gameId: Int,
    ) {
        viewModelScope.launch {
            println("::: PUT SHIP TO SERVER FUNCTION :::")
            _isLoading = true
            _finishPutShips = false
            try {
                error = null
                println("PUT SHIPS!!!")
                val putOrRemoveShips = localShips.toListPutOrRemoveShip()
                putOrRemoveShips.forEach {
                    service.putShipsInBoard(userId, token, gameId, it)
                }
            } catch (e: Exception) {
                error = e
            }
            _isLoading = false
            _finishPutShips = true
        }
    }

    fun initializeGame(userId: Int, token: String, gameId: Int) {
        println("::: INITIALIZE GAME FUNCTION :::")
        viewModelScope.launch {
            _isLoading = true
            _localGame =
                try {
                    error = null
                    val board =
                        if (_localGame?.getOrNull() != null) _localGame!!.getOrNull()!!.boards
                        else Boards()
                    Result.success(
                        service.getGameInfo(userId, token, gameId).toGame().copy(boards = board)
                    )
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun exitGame(userId: Int, token: String, gameId: Int) {
        viewModelScope.launch {
            _isLoading = true
            _exitGame =
                try {
                    error = null
                    Result.success(service.exitGame(userId, token, gameId))
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun setReady(userId: Int, token: String, gameId: Int) {
        println("::: SET GAME READY FUNCTION :::")
        viewModelScope.launch {
            _isLoading = true
            setReadyRes = try {
                error = null
                println(":::set ready:::")
                while (!finishPutShips) {
                    delay(700)
                }
                Result.success(service.setReady(userId, token, gameId).toGameAndSaveBoards(_localGame!!.getOrNull()!!))
            } catch (e: Exception) {
                error = e
                Result.failure(e)
            }
            _isLoading = false
            println("READY FINISH")
        }
    }

    fun getGameInfo(userId: Int, token: String, gameId: Int) {
        println("::: GET GAME INFO FUNCTION :::")
        viewModelScope.launch {
            _isLoading = true
            _localGame =
                try {
                    error = null
                    val server = service.getGameInfo(userId, token, gameId)
                    println(server)
                    val a = server.toGameAndSaveBoards(_localGame?.getOrNull()!!)
                    println(a)
                    Result.success(a)
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun getGameInfoPolling(userId: Int, token: String, gameId: Int) {
        println("::: GET GAME INFO POLLING FUNCTION :::")
        viewModelScope.launch {
            while (true) {
                _localGame =
                    try {
                        error = null
                        val server = service.getGameInfo(userId, token, gameId)
                        Result.success(server.toGameAndSaveBoards(_localGame?.getOrNull()!!))
                    } catch (e: Exception) {
                        error = e
                        Result.failure(e)
                    }
                delay(3000)
            }
        }
    }

    fun getGameInfoInBattle(userId: Int, token: String, gameId: Int) {
        println("::: GET GAME BATTLE INFO FUNCTION :::")
        viewModelScope.launch {
            _isLoading = true
            _localGame =
                try {
                    error = null
                    while (!finishShotShips) {
                        delay(500)
                    }
                    val server = service.getGameInfo(userId, token, gameId)
                    Result.success(server.toGameAndSaveBoards(_localGame?.getOrNull()!!))
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _finishShotShips = false
            _isLoading = false
        }
    }

    fun readyWaiting(userId: Int, token: String, gameId: Int, delayTime: Long) {
        println("::: READY WAITING FUNCTION :::")
        viewModelScope.launch {
            error = null
            while (localGame?.initialTurn == null && !readyWaitingExit) {
                val gameTemp =
                    try {

                        Result.success(service.getGameInfo(userId, token, gameId))
                    } catch (e: Exception) {
                        error = e
                        Result.failure(e)
                    }
                if (gameTemp.isSuccess && gameTemp.getOrNull()?.initialTurn != null) {
                    _localGame = Result.success(
                        _localGame?.getOrNull()!!.copyNonBoard(gameTemp.getOrNull()!!.toGame())
                    )
                }
                delay(delayTime)
            }
        }
    }

    fun waitingOther(userId: Int, token: String, gameId: Int, delayTime: Long) {
        println("::: WAITING OTHER FUNCTION :::")
        viewModelScope.launch {
            _isLoading = true
            error = null
            while (localGame?.turn != Player(userId) && !waitingOtherExit) {
                val gameTemp =
                    try {
                        Result.success(service.getGameInfo(userId, token, gameId))
                    } catch (e: Exception) {
                        error = e
                        Result.failure(e)
                    }
                if (gameTemp.isSuccess && gameTemp.getOrNull()?.turn == userId || gameTemp.getOrNull()?.winner != null) {
                    _localGame = Result.success(
                        gameTemp.getOrNull()!!.toGameAndSaveBoards(_localGame!!.getOrNull()!!)
                    )
                }
                delay(delayTime)
            }
            _isLoading = false
        }
    }

    fun shotOther(userId: Int, token: String, gameId: Int, position: Position) {
        println("::: SHOT OTHER FUNCTION :::")
        viewModelScope.launch {
            _isLoading = true
            _finishShotShips = false
            _shotResult =
                try {
                    error = null
                    shotPosition = position
                    Result.success(service.shot(userId, token, gameId, position))
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _finishShotShips = true
            _isLoading = false
        }
    }

    fun managerShotResult() {
        println("::: MANAGER SHOT FUNCTION :::")
        try {
            if (_shotResult?.getOrNull() != null) {

                error = null
                val shotRes = _shotResult?.getOrNull()!!
                if (shotRes.shotState === ShotState.SHOT) {
                    val grid = shotRes.gridCells
                    if (grid!!.size == 1) {
                        val pos = Position[grid.first().column - 1, grid.first().row - 1]
                        _localGame = Result.success(
                            _localGame!!.getOrNull()!!.updateEnigmaGrid(pos, GridCellState.SHOT)
                        )
                        return
                    } else {
                        grid.forEach {
                            val pos = Position[it.column - 1, it.row - 1]
                            _localGame = Result.success(
                                _localGame!!.getOrNull()!!.updateEnigmaGrid(pos, GridCellState.SINK)
                            )
                        }
                        return
                    }
                } else {
                    _localGame = Result.success(
                        _localGame!!.getOrNull()!!
                            .updateEnigmaGrid(shotPosition!!, GridCellState.MISS)
                    )
                }
            }
        } catch (e: Exception) {
            error = e
        }

    }

    // get server grid and process
    fun shotMyBoard(userId: Int, token: String, gameId: Int) {
        viewModelScope.launch {
            _isLoading = true
            val a =
                try {
                    error = null
                    Result.success(service.getMyGrid(userId, token, gameId))
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun getFinal(userId: Int, token: String, gameId: Int) {
        viewModelScope.launch {
            _isLoading = true
            _finalGetGame =
                try {
                    error = null
                    Result.success(
                        service.getGameInfo(userId, token, gameId)
                            .toGameAndSaveBoards(_localGame!!.getOrNull()!!)
                    )
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun endGame(userId: Int, token: String, gameId: Int) {
        viewModelScope.launch {
            _isLoading = true
            _finalGetGame =
                try {
                    error = null
                    Result.success(
                        service.endGame(userId, token, gameId)
                            .toGameAndSaveBoards(_localGame!!.getOrNull()!!)
                    )
                } catch (e: Exception) {
                    error = e
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun timeover(userId: Int, token: String, gameId: Int) {
        viewModelScope.launch {
            _isLoading = true
            try {
                error = null
                print("início vm timeover")
                service.timeover(userId, token, gameId)
                print("fim vm timeover")
            } catch (e: Exception) {
                error = e
            }
            _isLoading = false
        }
    }
}