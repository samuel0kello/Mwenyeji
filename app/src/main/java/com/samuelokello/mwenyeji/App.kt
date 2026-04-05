package com.samuelokello.mwenyeji

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.navigation.BottomNavigationBar
import com.samuelokello.mwenyeji.navigation.MwenyejiNavGraph
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    MwenyejiAppTheme {
        val navHostController = rememberNavController()
        // Observe current back stack entry as STATE so recomposition is triggered
        val navBackStackEntry by navHostController.currentBackStackEntryAsState()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navHostController,
                    navBackStackEntry = navBackStackEntry,
                )
            }
        ) { paddingValues ->
            MwenyejiNavGraph(navHostController)
        }
    }
}