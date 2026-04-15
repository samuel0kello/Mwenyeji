package com.samuelokello.mwenyeji.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

data class AppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/**
 * Standard Mwenyeji top bar.
 *
 * @param title         Screen title shown in the center/start slot.
 * @param onNavigateBack Called when the back arrow is tapped. Pass null to hide the arrow.
 * @param actions       Optional trailing icon buttons (e.g. search, filter).
 * @param backgroundColor Defaults to [MwenyejiTheme.colorScheme.surface].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: List<AppBarAction> = emptyList(),
    backgroundColor: Color = MwenyejiTheme.colorScheme.surface,
    titleStyle: TextStyle = MwenyejiTheme.typography.titleLarge,
    showDivider: Boolean = false,
) {
    val colors = MwenyejiTheme.colorScheme

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = titleStyle,
                    color = colors.onSurface,
                )
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    BackButton(onClick = onNavigateBack)
                }
            },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface,
                    actionIconContentColor = colors.onSurface,
                ),
        )

        if (showDivider) {
            HorizontalDivider(
                color = colors.border,
                thickness = 1.dp,
            )
        }
    }
}

/**
 * Two-line app bar with title and subtitle.
 *
 * @param title         Primary heading.
 * @param subtitle      Supporting text shown below the title.
 * @param subtitleColor Defaults to [MwenyejiTheme.colorScheme.onSurfaceVariant].
 * @param onNavigateBack Pass a lambda to show the back arrow, null to hide it.
 * @param actions       Optional trailing icon buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiHeaderBar(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    subtitleColor: Color = MwenyejiTheme.colorScheme.onSurfaceVariant,
    actions: List<AppBarAction> = emptyList(),
    backgroundColor: Color = MwenyejiTheme.colorScheme.surface,
    showDivider: Boolean = false,
) {
    val colors = MwenyejiTheme.colorScheme

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MwenyejiTheme.typography.headlineMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MwenyejiTheme.typography.bodySmall,
                        color = subtitleColor,
                    )
                }
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    BackButton(onClick = onNavigateBack)
                }
            },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface,
                    actionIconContentColor = colors.onSurface,
                ),
        )

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Large app bar with title, subtitle, and a composable content slot below.
 *
 * The [content] slot sits below the TopAppBar row — use it for a search
 * field, filter chips, or a tab row.
 *
 * @param bottomPadding Padding below the content slot. Defaults to 12.dp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiLargeHeaderBar(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    subtitleColor: Color = MwenyejiTheme.colorScheme.onSurfaceVariant,
    actions: List<AppBarAction> = emptyList(),
    backgroundColor: Color = MwenyejiTheme.colorScheme.surface,
    showDivider: Boolean = true,
    bottomPadding: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable () -> Unit = {},
) {
    val colors = MwenyejiTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        TopAppBar(
            title = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MwenyejiTheme.typography.headlineMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MwenyejiTheme.typography.bodySmall,
                        color = subtitleColor,
                    )
                }
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    BackButton(onClick = onNavigateBack)
                }
            },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface,
                    actionIconContentColor = colors.onSurface,
                ),
        )

        // Content slot — search field, chips, etc.
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding),
        ) {
            content()
        }

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Route detail top bar.
 *
 * @param from      Origin location name.
 * @param to        Destination location name.
 * @param via       Via description shown as subtitle (e.g. "via Uhuru Highway").
 * @param onNavigateBack Required — route detail always has a back arrow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiRouteBar(
    from: String,
    to: String,
    via: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<AppBarAction> = emptyList(),
    backgroundColor: Color = MwenyejiTheme.colorScheme.surface,
    bottomPadding: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable () -> Unit = {},
    showDivider: Boolean = true,
) {
    val colors = MwenyejiTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        TopAppBar(
            title = {
                Column(
                    modifier = Modifier.padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = from,
                            style = MwenyejiTheme.typography.titleMedium,
                            color = colors.onSurface,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "→",
                            style = MwenyejiTheme.typography.titleMedium,
                            color = colors.primary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = to,
                            style = MwenyejiTheme.typography.titleMedium,
                            color = colors.onSurface,
                        )
                    }
                    Text(
                        text = via,
                        style = MwenyejiTheme.typography.bodySmall,
                        color = colors.primary,
                    )
                }
            },
            navigationIcon = {
                BackButton(onClick = onNavigateBack)
            },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface,
                    actionIconContentColor = colors.onSurface,
                ),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding),
        ) {
            content()
        }

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Step progress top bar for multi-step flows.
 *
 * @param stepLabel     e.g. "Step 1 of 4 · Route"
 * @param title         Step heading e.g. "Where does this guide go?"
 * @param currentStep   1-based current step index.
 * @param totalSteps    Total number of steps.
 * @param onNavigateBack Back arrow always shown in step flows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiStepBar(
    stepLabel: String,
    title: String,
    currentStep: Int,
    totalSteps: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MwenyejiTheme.colorScheme.surface,
) {
    val colors = MwenyejiTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Back arrow + step label in one row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BackButton(onClick = onNavigateBack)

            Text(
                text = stepLabel,
                style = MwenyejiTheme.typography.labelSmall,
                color = colors.primary,
            )
        }

        // Segmented progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(totalSteps) { index ->
                val isComplete = index < currentStep
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .size(height = 3.dp, width = 0.dp)
                            .clip(
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(2.dp),
                            ).background(
                                if (isComplete) {
                                    colors.primary
                                } else {
                                    colors.outlineVariant
                                },
                            ),
                )
            }
        }

        // Step title
        Text(
            text = title,
            style = MwenyejiTheme.typography.headlineSmall,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MwenyejiTheme.colorScheme.surfaceContainerHigh)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ArrowBackIosNew,
            contentDescription = "Navigate back",
            tint = MwenyejiTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun AppBarIconButton(action: AppBarAction, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .padding(end = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MwenyejiTheme.colorScheme.surfaceContainerHigh)
                .clickable(role = Role.Button, onClick = action.onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
            tint = MwenyejiTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp),
        )
    }
}

// Previews
@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun TopBarPreview() {
    MwenyejiAppTheme {
        MwenyejiTopBar(
            title = "Your Profile",
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun HeaderBarPreview() {
    MwenyejiAppTheme {
        MwenyejiHeaderBar(
            title = "Where to?",
            subtitle = "Find local ways to move around Nairobi",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun LargeHeaderBarPreview() {
    MwenyejiAppTheme {
        MwenyejiLargeHeaderBar(
            title = "Where to?",
            subtitle = "Find local ways to move around Nairobi",
            content = {
                androidx.compose.material3.OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search area, stage, destination...") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun RouteBarPreview() {
    MwenyejiAppTheme {
        MwenyejiRouteBar(
            from = "CBD",
            to = "Westlands",
            via = "via Uhuru Highway",
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun StepBarPreview() {
    MwenyejiAppTheme {
        MwenyejiStepBar(
            stepLabel = "Step 2 of 4 · Timing",
            title = "When does this work best?",
            currentStep = 2,
            totalSteps = 4,
            onNavigateBack = {},
        )
    }
}
