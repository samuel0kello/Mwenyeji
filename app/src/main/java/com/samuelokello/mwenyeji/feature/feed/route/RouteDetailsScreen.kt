package com.samuelokello.mwenyeji.feature.feed.route

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.core.network.ConnectivityStatus
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.feature.feed.components.RouteTagChip
import com.samuelokello.mwenyeji.presentation.designsystem.animation.ExpandAnimatedVisibility
import com.samuelokello.mwenyeji.presentation.designsystem.animation.MwenyejiAnimatedVisibility
import com.samuelokello.mwenyeji.presentation.designsystem.animation.MwenyejiAnimations
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiLoadingIndicator
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiRouteBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.presentation.ui.shimmerEffect
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
    from: String? = null,
    to: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToContribute: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(routeId, from, to) {
        viewModel.onAction(RouteDetailsAction.LoadRoute(routeId, from, to))
    }

    LaunchedEffect(onNavigateBack) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RouteDetailsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MwenyejiLoadingIndicator()
            }
        }

        state.route == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.route_not_found),
                    color = MwenyejiTheme.colorScheme.onSurfaceVariant,
                )
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
    MwenyejiTheme.typography

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            Column {
                MwenyejiRouteBar(
                    from = state.overrideFrom ?: route.from,
                    to = state.overrideTo ?: route.to,
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
                                RouteTagChip(
                                    label =
                                        stringResource(
                                            R.string.stages_format,
                                            route.stopCount,
                                        ),
                                )
                            }
                            route.peakHeadwayMins?.let {
                                RouteTagChip(label = stringResource(R.string.peak_headway_format, it))
                            }
                        }
                    },
                )
                OfflineIndicator(status = state.connectivityStatus)
            }
        },
        bottomBar = {
            RouteDetailBottomBar(
                guideCount = route.guideCount,
                onNavigateToContribute = onNavigateToContribute,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + 12.dp,
                ),
        ) {
            // AI Suggestion — shown if generated
            state.suggestedGuide?.let { aiGuide ->
                item(key = "ai_suggestion") {
                    MwenyejiAnimatedVisibility(
                        visible = state.guides.isEmpty(),
                        enter =
                            MwenyejiAnimations.fadeIn +
                                MwenyejiAnimations.slideDownEnter,
                        exit =
                            MwenyejiAnimations.fadeOut +
                                MwenyejiAnimations.shrinkVertically,
                    ) {
                        SuggestedGuideCard(
                            guide = aiGuide,
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .animateItem(),
                        )
                    }
                }
            }

            item(key = "ai_shimmer") {
                // AI Loading Shimmer
                MwenyejiAnimatedVisibility(
                    visible = state.isGeneratingAi,
                    enter =
                        MwenyejiAnimations.fadeIn +
                            MwenyejiAnimations.slideDownEnter,
                    exit =
                        MwenyejiAnimations.fadeOut +
                            MwenyejiAnimations.shrinkVertically,
                ) {
                    AiGuideShimmer(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .animateItem(),
                    )
                }
            }

            item(key = "guides_loading") {
                ExpandAnimatedVisibility(
                    visible = state.isLoading,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .animateItem(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = colors.primary) }
                }
            }

            item(key = "no_guides") {
                MwenyejiAnimatedVisibility(
                    visible = state.guides.isEmpty() && state.suggestedGuide == null && !state.isGeneratingAi,
                    enter =
                        MwenyejiAnimations.fadeIn +
                            MwenyejiAnimations.slideUpEnter,
                    exit =
                        MwenyejiAnimations.fadeOut +
                            MwenyejiAnimations.shrinkVertically,
                ) {
                    NoGuidesYet(
                        routeNumber = route.routeNumber,
                        onNavigateToContribute = onNavigateToContribute,
                        onGenerateAi = { onAction(RouteDetailsAction.GenerateAiSuggestion) },
                        isAiGenerated = state.suggestedGuide != null,
                        aiError = state.aiError,
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .animateItem(),
                    )
                }
            }

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
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp)
                            .animateItem(),
                )
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Guide header — number + contributor + confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        if (totalGuides > 1) {
                            stringResource(R.string.guide_count_of_total, guideNumber, totalGuides)
                        } else {
                            stringResource(R.string.community_guide)
                        },
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_outline_star),
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.confirmed_count_format,
                                guide.confirmedCount,
                            ),
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
                    label = stringResource(R.string.verdict_works),
                    icon = R.drawable.ic_outline_check_circle,
                    accentColor = colors.success,
                    isSelected = selectedVerdict == RouteVerdict.WORKS,
                    onClick = { onVerdictSelected(RouteVerdict.WORKS) },
                    modifier = Modifier.weight(1f),
                )
                FeedbackButton(
                    label = stringResource(R.string.verdict_didnt),
                    icon = R.drawable.ic_outline_close,
                    accentColor = colors.error,
                    isSelected = selectedVerdict == RouteVerdict.DIDNT,
                    onClick = { onVerdictSelected(RouteVerdict.DIDNT) },
                    modifier = Modifier.weight(1f),
                )
                FeedbackButton(
                    label = stringResource(R.string.verdict_outdated),
                    icon = R.drawable.ic_outline_warning,
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
private fun NoGuidesYet(
    routeNumber: String?,
    onNavigateToContribute: () -> Unit,
    onGenerateAi: () -> Unit,
    isAiGenerated: Boolean,
    aiError: String? = null,
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
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (aiError != null) "AI Generation Failed" else stringResource(R.string.no_local_guides_yet),
                style = MwenyejiTheme.typography.titleMedium,
                color = if (aiError != null) colors.error else colors.onSurface,
                textAlign = TextAlign.Center,
            )
            val routeDesc =
                routeNumber?.let { stringResource(R.string.route_number_format, it) }
                    ?: stringResource(R.string.this_route)

            Text(
                text = aiError ?: stringResource(R.string.be_the_first_to_share_format, routeDesc),
                style = MwenyejiTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // AI Generate / Retry Button
                if (!isAiGenerated) {
                    MwenyejiCard(
                        onClick = onGenerateAi,
                        containerColor = colors.secondaryContainer,
                        elevation = MwenyejiTheme.elevation.level0,
                        border = BorderStroke(1.dp, if (aiError != null) colors.error else colors.secondary),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter =
                                    painterResource(
                                        if (aiError !=
                                            null
                                        ) {
                                            R.drawable.ic_outline_warning
                                        } else {
                                            R.drawable.ic_outline_data_exploration
                                        },
                                    ),
                                contentDescription = null,
                                tint = if (aiError != null) colors.error else colors.onSecondaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = if (aiError != null) "Retry AI Guide" else "Generate AI Guide",
                                style = MwenyejiTheme.typography.labelMedium,
                                color = if (aiError != null) colors.error else colors.onSecondaryContainer,
                            )
                        }
                    }
                }

//                // Contribute Button
//                MwenyejiCard(
//                    onClick = onNavigateToContribute,
//                    containerColor = colors.primary,
//                    elevation = MwenyejiTheme.elevation.level0,
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_outline_add),
//                            contentDescription = null,
//                            tint = colors.onPrimary,
//                            modifier = Modifier.size(16.dp),
//                        )
//                        Text(
//                            text = stringResource(R.string.add_the_first_guide),
//                            style = MwenyejiTheme.typography.labelMedium,
//                            color = colors.onPrimary,
//                        )
//                    }
//                }
            }
        }
    }
}

@Composable
private fun AiGuideShimmer(modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f)),
        elevation = MwenyejiTheme.elevation.level1,
        containerColor = colors.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(colors.surfaceContainerHigh, CircleShape)
                            .shimmerEffect(),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier =
                            Modifier
                                .size(width = 120.dp, height = 16.dp)
                                .background(colors.surfaceContainerHigh)
                                .shimmerEffect(),
                    )
                    Box(
                        modifier =
                            Modifier
                                .size(width = 80.dp, height = 12.dp)
                                .background(colors.surfaceContainerHigh)
                                .shimmerEffect(),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier =
                            Modifier
                                .weight(
                                    1f,
                                )
                                .height(48.dp)
                                .background(colors.surfaceContainerHigh, MwenyejiTheme.shapes.small)
                                .shimmerEffect(),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            40.dp,
                        )
                        .background(colors.surfaceContainerHigh, MwenyejiTheme.shapes.medium)
                        .shimmerEffect(),
            )
        }
    }
}

@Composable
private fun RouteDetailBottomBar(
    guideCount: Int,
    onNavigateToContribute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    LocalContext.current
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val guideCountText =
                when (guideCount) {
                    0 -> stringResource(R.string.no_guides_yet)
                    1 -> stringResource(R.string.one_guide)
                    else -> stringResource(R.string.guides_count_format, guideCount)
                }
            Text(
                text = guideCountText,
                style = MwenyejiTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
//            TextButton(onClick = onNavigateToContribute) {
//                Icon(
//                    painter = painterResource(R.drawable.ic_outline_add),
//                    contentDescription = null,
//                    tint = colors.primary,
//                    modifier = Modifier.size(16.dp),
//                )
//                Spacer(Modifier.size(4.dp))
//                Text(text = stringResource(R.string.add_guide), color = colors.primary)
//            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestedGuideCard(
    guide: Guide,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
        elevation = MwenyejiTheme.elevation.level1,
        containerColor = colors.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // AI Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .background(colors.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_outline_data_exploration),
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Mwenyeji AI", style = typography.titleSmall, color = colors.onSurface)
                            Box(
                                modifier =
                                    Modifier
                                        .background(colors.primary, CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(text = "SUGGESTED", style = typography.labelSmall, color = colors.onPrimary)
                            }
                        }
                        Text(text = "Based on travel patterns", style = typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }

            // Summary Grid (Fare | Sacco | Timing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoBox(
                    label = "FARE",
                    value = guide.formattedFare ?: "---",
                    modifier = Modifier.weight(1f),
                )
                InfoBox(
                    label = "SACCO",
                    value = guide.sacco.ifBlank { "Any" },
                    modifier = Modifier.weight(1f),
                )
                InfoBox(
                    label = "BEST",
                    value = guide.bestTimeOfDay.displayName,
                    modifier = Modifier.weight(1f),
                )
            }

            // AI Insight
            if (guide.timingReason.isNotBlank()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                colors.secondaryContainer.copy(alpha = 0.3f),
                                MwenyejiTheme.shapes.medium,
                            )
                            .padding(12.dp),
                ) {
                    Text(
                        text = "AI Insight — \"${guide.timingReason}\"",
                        style = typography.bodyMedium,
                        color = colors.onSecondaryContainer,
                    )
                }
            }

            // Tags
            if (guide.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    guide.tags.forEach { tag ->
                        RouteTagChip(label = tag.displayName, isPrimary = true)
                    }
                }
            }

            // Walkthrough
            if (guide.steps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WALKTHROUGH",
                        style = typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                    guide.steps.forEach { step ->
                        Step(stepNumber = "${step.order}", stepDescription = step.instruction)
                    }
                }
            }

            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)

            // AI Disclaimer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_outline_info),
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier =
                        Modifier
                            .size(14.dp)
                            .padding(top = 2.dp),
                )
                Text(
                    text = stringResource(R.string.ai_disclaimer),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography
    Column(
        modifier =
            modifier
                .background(colors.surfaceContainerHigh, MwenyejiTheme.shapes.small)
                .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = typography.labelSmall, color = colors.onSurfaceVariant)
        Text(text = value, style = typography.titleSmall, color = colors.primary)
    }
}

@Composable
private fun FeedbackButton(
    label: String,
    icon: Int,
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(text = label, style = MwenyejiTheme.typography.labelMedium, color = accentColor, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RouteHint(
    reason: String,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.works_best_when),
            style = MwenyejiTheme.typography.labelSmall,
            color = colors.primary,
        )
        Text(text = reason, style = MwenyejiTheme.typography.bodyMedium, color = colors.onSurface)
    }
}

@Composable
fun HowToNavigate(
    steps: List<RouteStep>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.how_to_do_it),
            style = MwenyejiTheme.typography.labelSmall,
            color = MwenyejiTheme.colorScheme.onSurfaceVariant,
        )
        steps.forEach { step -> Step(stepNumber = "${step.order}", stepDescription = step.instruction) }
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
            Text(text = stepNumber, style = MwenyejiTheme.typography.labelMedium, color = colors.onPrimary, textAlign = TextAlign.Center)
        }
        Text(text = stepDescription, style = MwenyejiTheme.typography.bodyMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_outline_warning),
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.local_warnings),
                    style = MwenyejiTheme.typography.labelSmall,
                    color = colors.warning,
                )
            }
            warning.lines().filter { it.isNotBlank() }.forEach { line ->
                Text(text = "• $line", style = MwenyejiTheme.typography.bodyMedium, color = colors.onWarningContainer)
            }
        }
    }
}

@Composable
private fun OfflineIndicator(status: ConnectivityStatus) {
    val isOffline = status == ConnectivityStatus.Lost || status == ConnectivityStatus.Unavailable

    ExpandAnimatedVisibility(
        visible = isOffline,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MwenyejiTheme.colorScheme.errorContainer)
                    .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_outline_warning),
                    contentDescription = null,
                    tint = MwenyejiTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Working offline",
                    style = MwenyejiTheme.typography.labelSmall,
                    color = MwenyejiTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

private fun Long.toRelativeTime(context: Context): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = diff / 3_360_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> context.getString(R.string.just_now)
        minutes < 60 -> context.getString(R.string.minutes_ago_short, minutes.toInt())
        hours < 24 -> context.getString(R.string.hours_ago_short, hours.toInt())
        else -> context.getString(R.string.days_ago_short, days.toInt())
    }
}
