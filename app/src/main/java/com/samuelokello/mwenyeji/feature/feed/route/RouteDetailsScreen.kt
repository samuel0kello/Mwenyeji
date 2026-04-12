package com.samuelokello.mwenyeji.feature.feed.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.feature.feed.components.RouteTagChip
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiRouteBar
import com.samuelokello.mwenyeji.ui.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import org.koin.compose.viewmodel.koinViewModel

enum class RouteVerdict(
    val firestoreValue: String,
) {
    WORKS("CONFIRMED"),
    DIDNT("DIDNT_WORK"),
    OUTDATED("OUTDATED"),
}

@Composable
fun RouteDetailsScreen(
    routeId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load route when screen opens
    LaunchedEffect(routeId) {
        viewModel.onAction(RouteDetailsAction.LoadRoute(routeId))
    }

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RouteDetailsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MwenyejiTheme.colorScheme.primary)
            }
        }

        state.route == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Route not found",
                    color = MwenyejiTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        else -> {
            RouteDetailsScreenContent(
                state = state,
                onAction = viewModel::onAction,
                onNavigateBack = onNavigateBack,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteDetailsScreenContent(
    state: RouteDetailsState,
    onAction: (RouteDetailsAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = state.route ?: return
    val colors = MwenyejiTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            MwenyejiRouteBar(
                from = route.from,
                to = route.to,
                via = route.via,
                onNavigateBack = onNavigateBack,
                content = {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        route.formattedFare?.let { RouteTagChip(label = it) }
                        route.tags.forEach { tag ->
                            RouteTagChip(
                                label = tag.displayName,
                                isPrimary = tag == RouteTag.FAST || tag == RouteTag.CHEAP,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            RouteDetailBottomBar(
                contributorName = route.contributorId,
                confirmedCount = route.confirmedCount,
                timeAgo = route.lastConfirmedAt?.toRelativeTime() ?: "recently",
                selectedVerdict = state.selectedVerdict,
                onVerdictSelected = { onAction(RouteDetailsAction.VerdictSelected(it)) },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (route.timingReason.isNotBlank()) {
                item(key = "hint") {
                    RouteHint(
                        reason = route.timingReason,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            item(key = "steps") {
                HowToNavigate(
                    steps = route.steps,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (route.warnings.isNotBlank()) {
                item(key = "warnings") {
                    Warning(
                        warning = route.warnings,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RouteDetailBottomBar(
    contributorName: String,
    confirmedCount: Int,
    timeAgo: String,
    selectedVerdict: RouteVerdict?,
    onVerdictSelected: (RouteVerdict) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.surface)
                .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Contributed by $contributorName · Confirmed $timeAgo by $confirmedCount people",
                style = MwenyejiTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FeedbackButton(
                label = "Works",
                icon = Icons.Outlined.Check,
                accentColor = colors.success,
                isSelected = selectedVerdict == RouteVerdict.WORKS,
                onClick = { onVerdictSelected(RouteVerdict.WORKS) },
                modifier = Modifier.weight(1f),
            )
            FeedbackButton(
                label = "Didn't",
                icon = Icons.Outlined.Close,
                accentColor = colors.error,
                isSelected = selectedVerdict == RouteVerdict.DIDNT,
                onClick = { onVerdictSelected(RouteVerdict.DIDNT) },
                modifier = Modifier.weight(1f),
            )
            FeedbackButton(
                label = "Outdated",
                icon = Icons.Outlined.Warning,
                accentColor = colors.warning,
                isSelected = selectedVerdict == RouteVerdict.OUTDATED,
                onClick = { onVerdictSelected(RouteVerdict.OUTDATED) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FeedbackButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiCard(
        modifier = modifier,
        onClick = onClick,
        containerColor =
            if (isSelected) {
                accentColor.copy(alpha = 0.12f)
            } else {
                colors.surfaceContainerHigh
            },
        border =
            BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else colors.border,
            ),
        elevation = MwenyejiTheme.elevation.level0,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MwenyejiTheme.typography.labelMedium,
                color = accentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Section composables ───────────────────────────────────────────────────────

@Composable
fun RouteHint(
    reason: String,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.border),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "WORKS BEST WHEN",
                style = MwenyejiTheme.typography.labelSmall,
                color = colors.primary,
            )
            Text(
                text = reason,
                style = MwenyejiTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
fun HowToNavigate(
    steps: List<RouteStep>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "HOW TO DO IT",
            style = MwenyejiTheme.typography.labelSmall,
            color = MwenyejiTheme.colorScheme.onSurfaceVariant,
        )
        steps.forEach { step ->
            Step(
                stepNumber = "${step.order}",
                stepDescription = step.instruction,
            )
        }
    }
}

@Composable
fun Step(
    stepNumber: String,
    stepDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .background(color = colors.primary, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stepNumber,
                style = MwenyejiTheme.typography.labelMedium,
                color = colors.onPrimary,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = stepDescription,
            style = MwenyejiTheme.typography.bodyMedium,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun Warning(
    warning: String,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.warning),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.warningContainer.copy(alpha = 0.15f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "LOCAL WARNINGS",
                    style = MwenyejiTheme.typography.labelSmall,
                    color = colors.warning,
                )
            }
            warning.lines().filter { it.isNotBlank() }.forEach { line ->
                Text(
                    text = "• $line",
                    style = MwenyejiTheme.typography.bodyMedium,
                    color = colors.onWarningContainer,
                )
            }
        }
    }
}

private fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}
