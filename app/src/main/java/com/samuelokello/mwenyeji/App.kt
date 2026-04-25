package com.samuelokello.mwenyeji

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.feature.auth.SessionEvent
import com.samuelokello.mwenyeji.feature.auth.SessionViewModel
import com.samuelokello.mwenyeji.feature.auth.SignInPromptSheet
import com.samuelokello.mwenyeji.navigation.MwenyejiNavGraph
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.MwenyejiSnackbarHost
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(snackbarManager: SnackBarManager, modifier: Modifier = Modifier, sessionViewModel: SessionViewModel = koinViewModel()) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val signInUi by sessionViewModel.signInUi.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        sessionViewModel.events.collect { event ->
            when (event) {
                is SessionEvent.ShowSuccess -> {
                    snackbarManager.showSuccess(event.message)
                }

                is SessionEvent.ShowError -> {
                    snackbarManager.showError(
                        message = event.message,
                        actionLabel = "Retry",
                        onAction = { sessionViewModel.requireAuth { /* retry gate */ } },
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MwenyejiNavGraph(
                navController = navController,
                onRequireAuth = sessionViewModel::requireAuth,
            )

            SignInPromptSheet(
                visible = signInUi.promptVisible,
                isLoading = signInUi.isLoading,
                onDismiss = sessionViewModel::dismissSignInPrompt,
                onGoogleSignIn = { sessionViewModel.signInWithGoogle(context) },
            )

            MwenyejiSnackbarHost(
                manager = snackbarManager,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            StatusBarScrim(modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
private fun StatusBarScrim(modifier: Modifier = Modifier) {
    val heightDp =
        with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this).toDp()
        }
    Spacer(
        modifier =
            modifier
                .fillMaxWidth()
                .height(heightDp)
                .background(MwenyejiTheme.colorScheme.surface),
    )
}
