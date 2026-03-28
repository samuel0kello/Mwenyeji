package com.samuelokello.mwenyeji.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.feature.feed.components.FeedScreen
import com.samuelokello.mwenyeji.feature.onboarding.navigation.OnBoarding
import com.samuelokello.mwenyeji.feature.onboarding.navigation.onBoarding
import kotlinx.serialization.Serializable

@Composable
fun MwenyejiNavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = OnBoarding
    ){
        mainGraph(navController)

        onBoarding(
            navController,
            onFinishOnBoarding = {
                Toast.makeText(context,"OnBoarding Finished", Toast.LENGTH_LONG).show()
                navController.navigate(Main) {
                    popUpTo<OnBoarding> { inclusive = true }
                }
            }

        )
    }
}

fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {
    navigation<Main>(
        startDestination = BottomScreenRoutes.Home
    ) {
        composable <BottomScreenRoutes.Home>{
            FeedScreen()
        }

        composable <BottomScreenRoutes.Contribute>{

        }
    }
}


@Serializable
data object Main