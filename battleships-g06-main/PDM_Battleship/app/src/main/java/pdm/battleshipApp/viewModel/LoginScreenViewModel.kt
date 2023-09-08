package pdm.battleshipApp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pdm.battleshipApp.domain.UserIdAndToken
import pdm.battleshipApp.service.Service

class LoginScreenViewModel(private val service: Service): ViewModel() {

    private var _userIdAndToken by
    mutableStateOf<Result<UserIdAndToken?>?>(null)

    private var _isLoading by
    mutableStateOf(false)

    val isLoading: Boolean
        get() = _isLoading

    val userIdAndToken: Result<UserIdAndToken?>?
        get() = _userIdAndToken

    fun setLogin(username: String, pass: String) {
        viewModelScope.launch {
            _isLoading = true
            _userIdAndToken =
                try {
                    Result.success(service.login(username, pass))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            _isLoading = false
        }
    }
}