package pdm.battleshipApp.activity

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import pdm.battleshipApp.DependenciesContainer
import pdm.battleshipApp.ui.notification.ErrorDialog
import pdm.battleshipApp.ui.screens.AuthorScreen
import pdm.battleshipApp.ui.screens.AuthorsScreenState
import pdm.battleshipApp.utils.RefreshingState
import pdm.battleshipApp.utils.viewModelInit
import pdm.battleshipApp.viewModel.AuthorsScreenViewModel
import java.io.IOException

class AuthorScreenActivity: ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }

    private val viewModel: AuthorsScreenViewModel by viewModels{
        viewModelInit{
            AuthorsScreenViewModel(dependencies.service)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (viewModel.authors == null){
                viewModel.fetchAuthors()
            }
            val loadingState: RefreshingState =
                if (viewModel.isLoading) RefreshingState.Refreshing
                else RefreshingState.Idle

            AuthorScreen(
                state = AuthorsScreenState(viewModel.authors?.getOrNull(), loadingState),
                onBackRequested = { finish() },
                onSendEmailRequested = { openSendEmail() },
            )
            if (viewModel.authors?.isFailure == true){
                ErrorMessage()
            }
        }
    }

    private fun openSendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(authorEmail))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            }

            startActivity(intent)
        }
        catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Failed to send email", e)
            Toast
                .makeText(
                    this,
                    "Non",
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }
    @Composable
    private fun ErrorMessage() {
        try { viewModel.authors?.getOrThrow() }
        catch (e: IOException) {
            ErrorDialog(e,
                networkOnDismiss = { viewModel.fetchAuthors() },
                serverOnDismiss = {finishAndRemoveTask()},
                appOnDismiss = {finishAndRemoveTask()}
            )
        }
    }
}

private const val authorEmail = "a48294@alunos.isel.pt"
private const val emailSubject = "About the Battleship App"