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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.feature.feed.components.RouteTagChip
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiRouteBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
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
    onNavigateToContribute: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        viewModel.onAction(RouteDetailsAction.LoadRoute(routeId))
    }

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
                Text("Route not found", color = MwenyejiTheme.colorScheme.onSurfaceVariant)
            }
        }

        else -> {
            RouteDetailsScreenContent(
                state = state,
                onAction = viewModel::onAction,
                onNavigateBack = onNavigateBack,
                onNavigateToContribute = { onNavigateToContribute(routeId) },
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
    onNavigateToContribute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = state.route ?: return
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

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
                    // Route number + stop count chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        route.routeNumber?.let { RouteTagChip(label = it, isPrimary = true) }
                        if (route.stopCount > 0) {
                            RouteTagChip(label = "${route.stopCount} stages")
                        }
                        route.peakHeadwayMins?.let {
                            RouteTagChip(label = "Every ${it}min peak")
                        }
                    }
                },
            )
        },
        bottomBar = {
            RouteDetailBottomBar(
                guideCount = route.guideCount,
                onNavigateToContribute = onNavigateToContribute,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding =
                androidx.compose.foundation.layout.PaddingValues(
                    vertical = 12.dp,
                ),
        ) {
            if (state.isLoading) {
                item(key = "guides_loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = colors.primary) }
                }
            } else if (state.guides.isEmpty()) {
                item(key = "no_guides") {
                    NoGuidesYet(
                        routeNumber = route.routeNumber,
                        onNavigateToContribute = onNavigateToContribute,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                itemsIndexed(
                    items = state.guides,
                    key = { _, guide -> guide.id },
                ) { index, guide ->
                    GuideCard(
                        guide = guide,
                        guideNumber = index + 1,
                        totalGuides = state.guides.size,
                        selectedVerdict =
                            if (state.selectedGuideId == guide.id) {
                                state.selectedVerdict
                            } else {
                                null
                            },
                        onVerdictSelected = { verdict ->
                            onAction(RouteDetailsAction.VerdictSelected(guide.id, verdict))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Guide card — one contributor's knowledge ──────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuideCard(
    guide: Guide,
    guideNumber: Int,
    totalGuides: Int,
    selectedVerdict: RouteVerdict?,
    onVerdictSelected: (RouteVerdict) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.border),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Guide header — number + contributor + confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (totalGuides > 1) "Guide $guideNumber of $totalGuides" else "Community guide",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "Confirmed ${guide.confirmedCount}×",
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = colors.border, thickness = 0.5.dp)

            // Fare + sacco + tags chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                guide.formattedFare?.let { RouteTagChip(label = it) }
                if (guide.sacco.isNotBlank()) RouteTagChip(label = guide.sacco)
                guide.tags.forEach { tag ->
                    RouteTagChip(
                        label = tag.displayName,
                        isPrimary = tag == RouteTag.FAST || tag == RouteTag.CHEAP,
                    )
                }
            }

            // Timing hint
            if (guide.timingReason.isNotBlank()) {
                RouteHint(reason = guide.timingReason)
            }

            // Steps
            if (guide.steps.isNotEmpty()) {
                HowToNavigate(steps = guide.steps)
            }

            // Warnings
            if (guide.warnings.isNotBlank()) {
                Warning(warning = guide.warnings)
            }

            HorizontalDivider(color = colors.border, thickness = 0.5.dp)

            // Verdict buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
}

@Composable
private fun NoGuidesYet(routeNumber: String?, onNavigateToContribute: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.border),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "No local guides yet",
                style = MwenyejiTheme.typography.titleMedium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Be the first to share how to navigate${
                    routeNumber?.let { " route $it" } ?: " this route"
                }.",
                style = MwenyejiTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            MwenyejiCard(
                onClick = onNavigateToContribute,
                containerColor = colors.primary,
                elevation = MwenyejiTheme.elevation.level0,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Add the first guide",
                        style = MwenyejiTheme.typography.labelMedium,
                        color = colors.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteDetailBottomBar(guideCount: Int, onNavigateToContribute: () -> Unit, modifier: Modifier = Modifier) {
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text =
                    if (guideCount > 0) {
                        "$guideCount guide${if (guideCount > 1) "s" else ""}"
                    } else {
                        "No guides yet"
                    },
                style = MwenyejiTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            TextButton(onClick = onNavigateToContribute) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text(text = "Add guide", color = colors.primary)
            }
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
        containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else colors.surfaceContainerHigh,
        border =
            BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else colors.border,
            ),
        elevation = MwenyejiTheme.elevation.level0,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(18.dp))
            Text(text = label, style = MwenyejiTheme.typography.labelMedium, color = accentColor, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RouteHint(reason: String, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "WORKS BEST WHEN", style = MwenyejiTheme.typography.labelSmall, color = colors.primary)
        Text(text = reason, style = MwenyejiTheme.typography.bodyMedium, color = colors.onSurface)
    }
}

@Composable
fun HowToNavigate(steps: List<RouteStep>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "HOW TO DO IT", style = MwenyejiTheme.typography.labelSmall, color = MwenyejiTheme.colorScheme.onSurfaceVariant)
        steps.forEach { step -> Step(stepNumber = "${step.order}", stepDescription = step.instruction) }
    }
}

@Composable
fun Step(stepNumber: String, stepDescription: String, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(color = colors.primary, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stepNumber, style = MwenyejiTheme.typography.labelMedium, color = colors.onPrimary, textAlign = TextAlign.Center)
        }
        Text(text = stepDescription, style = MwenyejiTheme.typography.bodyMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
    }
}

@Composable
fun Warning(warning: String, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.warning),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.warningContainer.copy(alpha = 0.15f),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(14.dp),
                )
                Text(text = "LOCAL WARNINGS", style = MwenyejiTheme.typography.labelSmall, color = colors.warning)
            }
            warning.lines().filter { it.isNotBlank() }.forEach { line ->
                Text(text = "• $line", style = MwenyejiTheme.typography.bodyMedium, color = colors.onWarningContainer)
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
