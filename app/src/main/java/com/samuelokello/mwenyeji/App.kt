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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.feature.auth.AuthState
import com.samuelokello.mwenyeji.feature.auth.SignInPromptSheet
import com.samuelokello.mwenyeji.navigation.MwenyejiNavGraph
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.MwenyejiSnackbarHost
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App(modifier: Modifier = Modifier, snackbarManager: SnackbarManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navHostController = rememberNavController()
    val authRepository: AuthRepository = koinInject()

    val authState by authRepository.authState().collectAsStateWithLifecycle(
        initialValue = AuthState.Anonymous,
    )

    var showSignInPrompt by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    // Callback to invoke after successful sign-in (e.g. navigate to contribute)
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize()) {
            MwenyejiNavGraph(
                navController = navHostController,
                onRequireAuth = { onAuthenticated ->
                    when (authState) {
                        is AuthState.SignedIn -> {
                            onAuthenticated()
                        }

                        else -> {
                            pendingAction = onAuthenticated
                            showSignInPrompt = true
                        }
                    }
                },
            )

            SignInPromptSheet(
                visible = showSignInPrompt,
                isLoading = isSigningIn,
                onDismiss = {
                    showSignInPrompt = false
                    pendingAction = null
                },
                onSignInSuccess = {},
                onGoogleSignIn = {
                    scope.launch {
                        isSigningIn = true
                        authRepository
                            .signInWithGoogle(context)
                            .onSuccess {
                                isSigningIn = false
                                showSignInPrompt = false
                                snackbarManager.showSuccess("Welcome! You can now contribute routes.")
                                pendingAction?.invoke()
                                pendingAction = null
                            }.onFailure { e ->
                                isSigningIn = false
                                snackbarManager.showError(
                                    message = e.message ?: "Sign-in failed. Please try again.",
                                    actionLabel = "Retry",
                                    onAction = { showSignInPrompt = true },
                                )
                            }
                    }
                },
            )

            MwenyejiSnackbarHost(
                manager = snackbarManager,
                modifier = modifier.align(Alignment.TopCenter),
            )

            Spacer(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .height(
                            with(LocalDensity.current) {
                                WindowInsets.statusBars.getTop(this).toDp()
                            },
                        ).background(MwenyejiTheme.colorScheme.surface)
                        .align(Alignment.TopStart),
            )
        }
    }
}
