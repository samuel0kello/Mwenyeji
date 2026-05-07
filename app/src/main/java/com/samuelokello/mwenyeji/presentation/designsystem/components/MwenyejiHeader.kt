package com.samuelokello.mwenyeji.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

data class AppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun mwenyejiTopBarColors(backgroundColor: Color = MwenyejiTheme.colorScheme.surface): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = backgroundColor,
        scrolledContainerColor = backgroundColor,
        navigationIconContentColor = MwenyejiTheme.colorScheme.onSurface,
        titleContentColor = MwenyejiTheme.colorScheme.onSurface,
        actionIconContentColor = MwenyejiTheme.colorScheme.onSurface,
    )

/**
 * Standard single-line top bar — for screens with a short title and optional actions.
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

    Column(modifier = modifier.background(backgroundColor)) {
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
                    BackIconButton(onClick = onNavigateBack)
                }
            },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors = mwenyejiTopBarColors(backgroundColor),
        )

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Two-line header — title with subtitle. Uses Material 3's MediumTopAppBar
 * which handles the title-subtitle hierarchy with correct heights.
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

    Column(modifier = modifier.background(backgroundColor)) {
        MediumTopAppBar(
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MwenyejiTheme.typography.headlineSmall,
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
                    BackIconButton(onClick = onNavigateBack)
                }
            },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors = mwenyejiTopBarColors(backgroundColor),
        )

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Large header with a content slot — for screens that need a search field
 * or filter chips below the title.
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
    bottomPadding: Dp = 16.dp,
    content: @Composable () -> Unit = {},
) {
    val colors = MwenyejiTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (onNavigateBack != null) {
                BackIconButton(onClick = onNavigateBack)
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Spacer(Modifier.width(4.dp))

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(top = 12.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MwenyejiTheme.typography.headlineMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MwenyejiTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }

            Row {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = bottomPadding),
        ) {
            content()
        }

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Route detail header — origin → destination with via subtitle.
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
    bottomPadding: Dp = 12.dp,
    content: @Composable () -> Unit = {},
    showDivider: Boolean = true,
) {
    val colors = MwenyejiTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth().background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        TopAppBar(
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = from,
                            style = MwenyejiTheme.typography.titleMedium,
                            color = colors.onSurface,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "→",
                            style = MwenyejiTheme.typography.titleMedium,
                            color = colors.primary,
                        )
                        Spacer(Modifier.width(8.dp))
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
            navigationIcon = { BackIconButton(onClick = onNavigateBack) },
            actions = {
                actions.forEach { action ->
                    AppBarIconButton(action = action)
                }
            },
            colors = mwenyejiTopBarColors(backgroundColor),
        )

        if (content != {}) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding),
            ) {
                content()
            }
        }

        if (showDivider) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/**
 * Step progress header for multi-step flows.
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
        modifier = modifier.fillMaxWidth().background(backgroundColor),
    ) {
        // Top app bar row keeps the same height as other top bars.
        TopAppBar(
            title = {
                Text(
                    text = stepLabel,
                    style = MwenyejiTheme.typography.labelLarge,
                    color = colors.primary,
                )
            },
            navigationIcon = { BackIconButton(onClick = onNavigateBack) },
            colors = mwenyejiTopBarColors(backgroundColor),
        )

        // Progress + step title block
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isComplete) colors.primary else colors.outlineVariant,
                                ),
                    )
                }
            }

            Text(
                text = title,
                style = MwenyejiTheme.typography.headlineSmall,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun BackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        colors =
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MwenyejiTheme.colorScheme.surfaceContainerHigh,
                contentColor = MwenyejiTheme.colorScheme.onSurface,
            ),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Navigate back",
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
