package com.samuelokello.mwenyeji.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.samuelokello.mwenyeji.feature.onboarding.navigation.OnBoarding
import com.samuelokello.mwenyeji.feature.onboarding.navigation.onBoarding

@Composable
fun MwenyejiNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = OnBoarding
    ){
        onBoarding(
            navController,
            onFinishOnBoarding = {
                Toast.makeText(context,"OnBoarding Finished", Toast.LENGTH_LONG).show()
            }

        )
    }
}