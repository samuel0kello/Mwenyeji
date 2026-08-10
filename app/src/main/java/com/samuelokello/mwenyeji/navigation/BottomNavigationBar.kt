package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.feed.navigation.FeedsGraph
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    onContributeTapped: () -> Unit,
    isContributeSheetOpen: Boolean = false,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val show = navController.shouldShowBottomBar()

    // Only Home is a real nav item now
    val screens = listOf(BottomBarScreen.Home)

    AppBottomNavigationBar(
        show = show,
        onContributeTapped = onContributeTapped,
        isContributeSheetOpen = isContributeSheetOpen,
    ) {
        // Left side: Home item
        screens.forEach { item ->
            val isSelected =
                currentDestination?.hierarchy?.any {
                    it.hasRoute<FeedsGraph>()
                } ?: false

            AppBottomNavigationBarItem(
                icon = item.defaultIcon,
                selectedIcon = item.selectedIcon,
                label = item.title,
                selected = isSelected,
                onClick = {
                    navController.navigate(FeedsGraph) {
                        popUpTo<Main> { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    show: Boolean,
    onContributeTapped: () -> Unit,
    isContributeSheetOpen: Boolean,
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
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(65.dp),
                ) {
                    // Regular nav items in a row
                    Row(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .selectableGroup(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left half for nav items
                        Row(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            content = content,
                        )
                        // Right half is empty — FAB sits over it
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // FAB floats in the center-right of the bar
                    ContributeFab(
                        isOpen = isContributeSheetOpen,
                        onClick = onContributeTapped,
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 40.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContributeFab(
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 45f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "fabRotation",
    )

    Box(
        modifier =
            modifier
                .size(48.dp)
                .clickable(onClick = onClick)
                // Lifts the FAB above the bar visually
                .offset(y = (-10).dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = MwenyejiTheme.colorScheme.primary,
                    spotColor = MwenyejiTheme.colorScheme.primary,
                )
                .clip(RoundedCornerShape(14.dp))
                .background(MwenyejiTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_outline_add),
            contentDescription = if (isOpen) "Close contribute sheet" else "Contribute a guide",
            tint = MwenyejiTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = rotation },
        )
    }
}

@Composable
fun RowScope.AppBottomNavigationBarItem(
    modifier: Modifier = Modifier,
    icon: Int,
    selectedIcon: Int,
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
        targetValue =
            if (selected) {
                MwenyejiTheme.colorScheme.primary
            } else {
                MwenyejiTheme.colorScheme.outline
            },
        label = "itemColor",
    )
    val isShowingSelected = rotation > 90f
    val iconRotation = if (isShowingSelected) rotation - 180f else rotation

    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .weight(1f)
                .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 8 * density
                    },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter =
                    if (isShowingSelected) {
                        painterResource(selectedIcon)
                    } else {
                        painterResource(
                            icon,
                        )
                    },
                contentDescription = label,
                tint = color,
                modifier =
                    Modifier
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
