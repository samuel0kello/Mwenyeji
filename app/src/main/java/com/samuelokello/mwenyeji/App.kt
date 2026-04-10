package com.samuelokello.mwenyeji

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.feature.contribute.ContributeSheet
import com.samuelokello.mwenyeji.navigation.MwenyejiNavGraph
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.MwenyejiSnackbarHost
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme
import org.koin.compose.koinInject

@Composable
fun App(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val snackbarManager: SnackbarManager = koinInject()

    MwenyejiAppTheme {
        val navHostController = rememberNavController()
        var showContributeSheet by rememberSaveable { mutableStateOf(false) }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showContributeSheet = true }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Contribute")
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MwenyejiNavGraph(navController = navHostController)

                    ContributeSheet(
                        visible = showContributeSheet,
                        onDismiss = { showContributeSheet = false },
                        onNavigateToSuccess = {
                            showContributeSheet = false
                            snackbarManager.showSuccess("Guide submitted! Thank you 🙌")
                        },
                    )

                    MwenyejiSnackbarHost(
                        manager = snackbarManager,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
    }
}