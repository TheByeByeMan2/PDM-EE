package pdm.battleshipApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.ui.notification.ErrorDialog
import pdm.battleshipApp.ui.screens.RankingScreen
import pdm.battleshipApp.ui.screens.RankingScreenState
import pdm.battleshipApp.utils.RefreshingState
import pdm.battleshipApp.utils.viewModelInit
import pdm.battleshipApp.viewModel.RankingScreenViewModel
import java.io.IOException


class RankingScreenActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }

    private val viewModel: RankingScreenViewModel by viewModels {
        viewModelInit {
            RankingScreenViewModel(dependencies.service)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (viewModel.ranking == null) {
                viewModel.fetchRanking()
            }
            val loadingState: RefreshingState =
                if (viewModel.isLoading) RefreshingState.Refreshing
                else RefreshingState.Idle

            RankingScreen(
                state = RankingScreenState(viewModel.ranking?.getOrNull(), loadingState),
                onBackRequested = { finish() },
                onUpdateRequest = { viewModel.fetchRanking(forcedRefresh = true) },
            )
            if (viewModel.ranking?.isFailure == true)
                ErrorMessage()
        }
    }

    @Composable
    private fun ErrorMessage() {
        try {
            viewModel.ranking?.getOrThrow()
        } catch (e: IOException) {
            ErrorDialog(e,
                networkOnDismiss = { viewModel.fetchRanking() },
                serverOnDismiss = {finishAndRemoveTask()},
                appOnDismiss = {finishAndRemoveTask()}
            )
        }
    }
}