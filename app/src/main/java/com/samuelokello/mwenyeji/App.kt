package com.samuelokello.mwenyeji

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.MwenyejiSnackbarHost
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    snackbarManager: com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager,
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
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
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
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
                modifier = Modifier.align(Alignment.BottomCenter),
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
                .background(com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme.colorScheme.surface),
    )
}
