package pdm.battleshipApp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import pdm.battleshipApp.domain.UserComplexInfoRes
import pdm.battleshipApp.domain.WaitingRoom
import pdm.battleshipApp.domain.WaitingRoomMsg
import pdm.battleshipApp.service.Service

class WaitingRoomScreenViewModel(private val service: Service) : ViewModel() {

    private var wantExit by mutableStateOf(false)

    private var _exited by mutableStateOf<Result<Int>?>(null)
    val exited: Result<Int>? get() = _exited

    private var _isLoading by
    mutableStateOf(false)

    val isLoading: Boolean
        get() = _isLoading

    private var _waitingRoom by
    mutableStateOf<Result<WaitingRoom>?>(null)

    val waitingRoom: Result<WaitingRoom>?
        get() = _waitingRoom

    private var waitingRoomMonitor: Job? = null
    private var _waitingRoomMsg by mutableStateOf<WaitingRoomMsg?>(null)
    val waitingRoomMsg: WaitingRoomMsg?
        get() = _waitingRoomMsg



    fun matchMaking(userId: Int, token: String) {
        println("POST WAITING ROOM ----------")
        viewModelScope.launch {
            _isLoading = true
            _waitingRoom =
                try {
                    Result.success(service.postWaitingRoom(userId, token))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            _isLoading = false
        }
    }

    fun getWaitingRoom(userId: Int, token: String, delayTime: Long) {
        println("GET WAITING ROOM ----------")
        viewModelScope.launch {
            _isLoading = true
            while (waitingRoom?.getOrNull()?.gameId == null && !wantExit) {
                val waitingRoomTemp =
                    try {
                        Result.success(service.getWaitingRoom(userId, token))
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                if (waitingRoomTemp.isSuccess && waitingRoomTemp.getOrNull()?.gameId != null) {
                    _waitingRoom = waitingRoomTemp
                }
                delay(delayTime)
            }
            _isLoading = false
        }
    }

    fun exitWaitingRoom(userId: Int, token: String) {
        viewModelScope.launch {
            _isLoading = true
            _exited =
                try {
                    Result.success(service.exitWaitingRoom(userId, token))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            _isLoading = false
        }
        waitingRoomMonitor?.cancel()
        waitingRoomMonitor = null
        wantExit = true
    }

    fun waitingRoomListener(userId: Int): Job? =
        if (waitingRoomMonitor == null) {
            waitingRoomMonitor = viewModelScope.launch {
                println("listener !!!!!")
                service.waitingRoomListener().collect { msg ->
                    if (msg.userId != userId) {
                        _waitingRoomMsg = msg
                    }
                }
            }
            waitingRoomMonitor
        } else null

    fun waitingRoomSender(msg: WaitingRoomMsg){
        viewModelScope.launch {
            _isLoading = true
            val a =
                    try {
                        Result.success(service.waitingRoomSender(msg))
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
            _isLoading = false
        }
    }

}