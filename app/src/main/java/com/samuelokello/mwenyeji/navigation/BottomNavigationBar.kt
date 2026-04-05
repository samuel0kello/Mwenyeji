package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.samuelokello.mwenyeji.feature.feed.navigation.FeedsGraph
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
) {
    // Observe back stack as state so recomposition fires on every navigation event
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf(BottomBarScreen.Home, BottomBarScreen.Contribute)

    val show = navController.shouldShowBottomBar()

    AppBottomNavigationBar(show = show) {
        screens.forEach { item ->
            val isSelected = when (item.route) {
                BottomScreenRoutes.Home -> currentDestination?.hierarchy?.any {
                    it.hasRoute<FeedsGraph>()
                } ?: false

                BottomScreenRoutes.Contribute -> currentDestination?.hierarchy?.any {
                    it.hasRoute<BottomScreenRoutes.Contribute>()
                } ?: false
            }

            AppBottomNavigationBarItem(
                icon = item.defaultIcon,
                selectedIcon = item.selectedIcon,
                label = item.title,
                selected = isSelected,
                onClick = { navigateBottomBar(navController, item.route) },
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    show: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = MwenyejiTheme.colorScheme.surface,
        contentColor = MwenyejiTheme.colorScheme.onSurface,
        modifier = modifier.windowInsetsPadding(BottomAppBarDefaults.windowInsets),
    ) {
        if (show) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MwenyejiTheme.colorScheme.border,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }
        }
    }
}

@Composable
fun RowScope.AppBottomNavigationBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (selected) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "iconRotation",
    )
    val color by animateColorAsState(
        targetValue = if (selected) MwenyejiTheme.colorScheme.primary
        else MwenyejiTheme.colorScheme.outline,
        label = "itemColor",
    )

    val isShowingSelected = rotation > 90f
    val iconRotation = if (isShowingSelected) rotation - 180f else rotation

    Column(
        modifier = modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isShowingSelected) selectedIcon else icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationY = iconRotation },
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = color,
        )
    }
}

private fun navigateBottomBar(
    navController: NavController,
    destination: BottomScreenRoutes,
) {
    val target: Any = when (destination) {
        BottomScreenRoutes.Home -> FeedsGraph
        BottomScreenRoutes.Contribute -> BottomScreenRoutes.Contribute
    }
    navController.navigate(target) {
        popUpTo<Main> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}