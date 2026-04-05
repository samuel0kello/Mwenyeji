package com.samuelokello.mwenyeji

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.navigation.BottomNavigationBar
import com.samuelokello.mwenyeji.navigation.MwenyejiNavGraph
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    MwenyejiAppTheme {
        val navHostController = rememberNavController()

        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                // BottomNavigationBar now observes its own back stack state internally.
                // No need to hoist navBackStackEntry up to App — that caused a one-frame
                // lag where the bar would briefly appear/disappear on navigation.
                BottomNavigationBar(navController = navHostController)
            },
        ) { paddingValues ->
            MwenyejiNavGraph(
                navController = navHostController,
                // Pass paddingValues if your NavGraph root needs it,
                // otherwise individual screens handle their own padding
                // via Scaffold's paddingValues in each screen composable.
            )
        }
    }
}