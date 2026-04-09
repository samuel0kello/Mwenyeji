package com.samuelokello.mwenyeji.feature.contribute.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
data object ContributeNavGraph

@Serializable
data object ContributeRoute

fun NavGraphBuilder.contributeNavGraph() {
    navigation<ContributeNavGraph>(startDestination = ContributeRoute) {
        composable<ContributeRoute> {

        }
    }
}