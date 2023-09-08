package pdm.battleshipApp.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import pdm.battleshipApp.domain.UserDetail
import pdm.battleshipApp.service.Service

class RankingScreenViewModel(private val service: Service): ViewModel() {

    private var _isLoading by
    mutableStateOf(false)

    val isLoading: Boolean
        get() = _isLoading

    private var _ranking by
    mutableStateOf<Result<List<UserDetail>>?>(null)

    val ranking: Result<List<UserDetail>>?
        get() = _ranking

    fun fetchRanking(forcedRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading = true
            _ranking =
                try {
                    Result.success(service.fetchRanking(
                        if (forcedRefresh)Service.Mode.FORCE_REMOTE
                    else Service.Mode.AUTO
                    ))
                }
                catch (e: Exception){
                    Result.failure(e)
                }
            _isLoading = false
        }
    }
}
