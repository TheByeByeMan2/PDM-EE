package pdm.battleshipApp.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pdm.battleshipApp.domain.User
import pdm.battleshipApp.service.Service

class RegisterScreenViewModel(private val service: Service): ViewModel() {

    private var _isLoading by
    mutableStateOf(false)

    val isLoading: Boolean
    get() = _isLoading

    private var _user by
    mutableStateOf<Result<User>?>(null)

    val user: Result<User>?
    get() = _user

    fun register(username:String, pass: String) {
        viewModelScope.launch {
            _isLoading = true
            _user =
                try {
                    Result.success(service.register(username, pass))
                }
                catch (e: Exception){
                    Result.failure(e)
                }
            _isLoading = false
        }
    }
}