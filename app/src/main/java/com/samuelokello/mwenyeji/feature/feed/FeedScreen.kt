package com.samuelokello.mwenyeji.feature.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.feature.contribute.ContributeSheet
import com.samuelokello.mwenyeji.feature.feed.components.RouteCard
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiLargeHeaderBar
import com.samuelokello.mwenyeji.ui.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedScreen(
    onNavigateToRouteDetail: (String) -> Unit,
    onNavigateToSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FeedEffect.NavigateToRouteDetail -> {
                    onNavigateToRouteDetail(effect.route.id)
                }

                is FeedEffect.NavigateToSeeAll -> {
                    onNavigateToSeeAll()
                }

                is FeedEffect.ShowError -> { /* show snackbar if needed */
                }
            }
        }
    }

    FeedScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun FeedScreenContent(
    state: FeedState,
    onIntent: (FeedIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography
    var showContributeSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarManager: SnackbarManager = koinInject()

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            MwenyejiLargeHeaderBar(
                title = "Where to?",
                subtitle = "Find local ways to move around Nairobi",
                content = {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onIntent(FeedIntent.SearchQueryChanged(it)) },
                        placeholder = {
                            Text(
                                text = "Search area, stage, destination...",
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showContributeSheet = true },
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Contribute")
            }
        },
    ) { paddingValues ->

        when {
            state.isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            state.error != null -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error,
                        style = typography.bodyMedium,
                        color = colors.error,
                    )
                    TextButton(onClick = { onIntent(FeedIntent.RetryClicked) }) {
                        Text("Retry", color = colors.primary)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    // Time of day filter chips
                    item(key = "time_filters") {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = TimeOfDay.entries,
                                key = { it.name },
                            ) { timeOfDay ->
                                TimeOfDayChip(
                                    title = timeOfDay.displayName,
                                    selected = state.selectedTimeOfDay == timeOfDay,
                                    onSelected = {
                                        onIntent(FeedIntent.SelectTimeOfDay(timeOfDay))
                                    },
                                )
                            }
                        }
                        HorizontalDivider(
                            color = colors.border,
                            thickness = 1.dp,
                        )
                    }

                    // Section header
                    item(key = "section_header") {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Near CBD Now",
                                style = typography.titleSmall,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = { onIntent(FeedIntent.SeeAllClicked) },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = "See all",
                                    style = typography.labelMedium,
                                    color = colors.primary,
                                )
                            }
                        }
                    }

                    // Route cards
                    if (state.filteredRoutes.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No routes found for this time.\nTry a different filter.",
                                    style = typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        items(
                            items = state.filteredRoutes,
                            key = { it.id },
                        ) { route ->
                            RouteCard(
                                route = route,
                                onClick = { onIntent(FeedIntent.RouteClicked(route)) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }

        ContributeSheet(
            visible = showContributeSheet,
            onDismiss = { showContributeSheet = false },
            onNavigateToSuccess = {
                showContributeSheet = false
                snackbarManager.showSuccess("Guide submitted! Thank you 🙌")
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun FeedScreenContentPreview() {
    MwenyejiAppTheme {
        FeedScreenContent(
            state =
                FeedState(
                    selectedTimeOfDay = TimeOfDay.MORNING_RUSH,
                    filteredRoutes = emptyList(),
                ),
            onIntent = {},
        )
    }
}

@Composable
fun TimeOfDayChip(
    title: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSelected: (String) -> Unit = {},
) {
    val colors = MwenyejiTheme.colorScheme

    // Animate border and text color transitions
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.outlineVariant,
        animationSpec = tween(durationMillis = 150),
        label = "chipBorderColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurface,
        animationSpec = tween(durationMillis = 150),
        label = "chipContentColor",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else colors.surface,
        animationSpec = tween(durationMillis = 150),
        label = "chipContainerColor",
    )

    MwenyejiCard(
        modifier = modifier,
        onClick = { onSelected(title) },
        containerColor = containerColor,
        border =
            BorderStroke(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .height(44.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MwenyejiTheme.typography.labelMedium,
                color = contentColor,
            )
        }
    }
}

@Composable
fun TimeOfDayChipGroup(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            TimeOfDayChip(
                title = option,
                selected = option == selectedOption,
                onSelected = onOptionSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun TimeOfDayChipPreview() {
    MwenyejiAppTheme {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TimeOfDayChip(
                title = "Morning rush",
                selected = true,
                modifier = Modifier.weight(1f),
            )
            TimeOfDayChip(
                title = "Midday",
                selected = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun TimeOfDayChipGroupPreview() {
    MwenyejiAppTheme {
        var selected by remember { mutableStateOf("Morning rush") }
        TimeOfDayChipGroup(
            options = listOf("Morning rush", "Midday", "Evening", "Late night"),
            selectedOption = selected,
            onOptionSelected = { selected = it },
            modifier = Modifier.padding(16.dp),
        )
    }
}
