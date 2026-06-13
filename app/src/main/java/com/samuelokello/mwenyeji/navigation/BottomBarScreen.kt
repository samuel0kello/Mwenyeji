package com.samuelokello.mwenyeji.navigation

import com.samuelokello.mwenyeji.R
import kotlinx.serialization.Serializable

sealed class BottomBarScreen(
    val route: BottomScreenRoutes,
    var title: String,
    val defaultIcon: Int,
    val selectedIcon: Int,
) {
    data object Home : BottomBarScreen(
        route = BottomScreenRoutes.Home,
        title = "feed",
        defaultIcon = R.drawable.ic_outline_location_on,
        selectedIcon = R.drawable.ic_baseline_location_on,
    )
}

@Serializable
sealed interface BottomScreenRoutes {
    @Serializable
    data object Home : BottomScreenRoutes

    @Serializable
    data object Contribute : BottomScreenRoutes

    @Serializable
    data object Profile
}
