package com.samuelokello.mwenyeji.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed class BottomBarScreen(
    val route: BottomScreenRoutes,
    var title: String,
    val defaultIcon: ImageVector,
    val selectedIcon: ImageVector,
) {
    data object Home : BottomBarScreen(
        route = BottomScreenRoutes.Home,
        title = "feed",
        defaultIcon = Icons.Outlined.LocationOn,
        selectedIcon = Icons.Default.LocationOn,
    )
}

@Serializable
sealed interface BottomScreenRoutes {
    @Serializable
    data object Home : BottomScreenRoutes

    @Serializable
    data object Contribute : BottomScreenRoutes

    @Serializable
    data object profile
}
