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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.navigation.MwenyejiNavGraph
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.MwenyejiSnackbarHost
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun App(modifier: Modifier = Modifier, snackbarManager: SnackbarManager) {
    val navHostController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize()) {
            MwenyejiNavGraph(navController = navHostController)

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
