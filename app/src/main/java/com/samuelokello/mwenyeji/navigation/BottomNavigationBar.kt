package com.samuelokello.mwenyeji.navigation

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val homeItem = BottomBarScreen.Home
    val contributeItem = BottomBarScreen.Contribute

    val screens = listOf(
        homeItem,
        contributeItem
    )

    AppBottomNavigationBar(
        show = navController.shouldShowBottomBar()
    ) {
        screens.forEach { item ->
            val currentDestination = navController.currentBackStackEntry?.destination
            val isSelected = when(item.route){
                BottomScreenRoutes.Contribute -> currentDestination.isRoute<BottomScreenRoutes.Contribute>()
                BottomScreenRoutes.Home -> currentDestination.isRoute<BottomScreenRoutes.Home>()
            }

            AppBottomNavigationBarItem(
                icon = item.defaultIcon,
                selectedIcon = item.selectedIcon,
                label = item.title,
                onClick = { navigateBottomBar(navController, item.route) },
                selected = isSelected
            )
        }
    }
}

inline fun <reified T : Any> NavDestination?.isRoute(): Boolean {
    return this?.hasRoute<T>() == true
}

@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    show: Boolean,
    content: @Composable (RowScope.() -> Unit),
) {
    Surface(
        color = MwenyejiTheme.colorScheme.background,
        contentColor = MwenyejiTheme.colorScheme.onBackground,
        modifier = modifier.windowInsetsPadding(BottomAppBarDefaults.windowInsets),
    ) {
        if(show) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().height(1.dp),
                    color = MwenyejiTheme.colorScheme.outline.copy(alpha = 0.2f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().height(65.dp).selectableGroup(),
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
    @DrawableRes icon: Int,
    @DrawableRes selectedIcon: Int,
    label: String,
    onClick: () -> Unit,
    selected: Boolean,
) {

    val rotation by animateFloatAsState(
        targetValue = if (selected) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "iconRotation"
    )

    val color by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }
    )

    val isShowingSelected = rotation > 90f
    val iconRotation = if (isShowingSelected) rotation - 180f else rotation

    Column(
        modifier =
            modifier.fillMaxHeight()
                .weight(1f)
                .clickable(
                    onClick = onClick,
                ),
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
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(if (isShowingSelected) selectedIcon else icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationY = iconRotation
                    },
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight =
                if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Medium
                },
            color = color,
        )
    }
}

private fun navigateBottomBar(
    navController: NavController,
    destination: BottomScreenRoutes,
) {
    navController.navigate(destination) {
        navController.graph.startDestinationRoute?.let { route ->
            popUpTo<BottomScreenRoutes.Home> {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}