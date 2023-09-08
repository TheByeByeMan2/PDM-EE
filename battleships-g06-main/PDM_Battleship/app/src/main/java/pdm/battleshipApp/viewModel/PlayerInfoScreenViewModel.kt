package pdm.battleshipApp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pdm.battleshipApp.domain.LocalData
import pdm.battleshipApp.domain.UserComplexInfoRes
import pdm.battleshipApp.domain.UserDetail
import pdm.battleshipApp.service.Service

class PlayerInfoScreenViewModel(private val service: Service): ViewModel() {

    private var _isLoading by
    mutableStateOf(false)

    val isLoading: Boolean
        get() = _isLoading

    private var _userDetail by
    mutableStateOf<Result<UserDetail?>?>(null)

    val userDetail: Result<UserDetail?>?
        get() = _userDetail

    fun fetchUserInfo(userId: Int, token: String) {
        viewModelScope.launch {
            _isLoading = true
            _userDetail =
                try {
                    Result.success(service.fetchUserInfo(userId, token))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            _isLoading = false
        }
    }
}