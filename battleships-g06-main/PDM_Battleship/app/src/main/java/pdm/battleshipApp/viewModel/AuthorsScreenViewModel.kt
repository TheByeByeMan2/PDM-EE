package pdm.battleshipApp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pdm.battleshipApp.domain.Authors
import pdm.battleshipApp.service.Service

class AuthorsScreenViewModel(private val service: Service): ViewModel() {

    private var _isLoading by
    mutableStateOf(false)

    val isLoading: Boolean
        get() = _isLoading

    private var _authors by
    mutableStateOf<Result<Authors>?>(null)

    val authors: Result<Authors>?
        get() = _authors

    fun fetchAuthors() {
        viewModelScope.launch {
            _isLoading = true
            _authors =
                try {
                    Result.success(service.fetchAuthors())
                }
                catch (e: Exception){ Result.failure(e)}
            _isLoading = false
        }
    }
}