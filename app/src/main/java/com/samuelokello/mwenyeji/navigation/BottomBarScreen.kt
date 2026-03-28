package com.samuelokello.mwenyeji.navigation

import androidx.annotation.DrawableRes
import com.samuelokello.mwenyeji.R
import kotlinx.serialization.Serializable

sealed class BottomBarScreen(
    val route: BottomScreenRoutes,
    var title: String,
    @DrawableRes val defaultIcon: Int,
    @DrawableRes val selectedIcon: Int
) {
    data object Home : BottomBarScreen(
        route = BottomScreenRoutes.Home,
        title = "feed",
        defaultIcon = R.drawable.ic_navigation,
        selectedIcon = R.drawable.ic_navigation
    )
    data object Contribute : BottomBarScreen(
        route = BottomScreenRoutes.Contribute,
        title = "feed",
        defaultIcon = R.drawable.ic_navigation,
        selectedIcon = R.drawable.ic_navigation
    )
}

@Serializable
sealed interface BottomScreenRoutes {

    @Serializable
    data object Home: BottomScreenRoutes

    @Serializable
    data object Contribute: BottomScreenRoutes

    @Serializable
    data object profile
}