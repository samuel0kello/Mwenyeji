package com.samuelokello.mwenyeji.feature.feed

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.feature.feed.components.RouteCard
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiLargeHeaderBar
import com.samuelokello.mwenyeji.ui.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.ui.designsystem.components.toolTip.MwenyejiTooltip
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("MissingPermission")
@Composable
fun FeedScreen(
    onNavigateToRouteDetail: (String) -> Unit,
    onNavigateToSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel(),
    onNavigateToContribute: () -> Unit,
    snackBarManager: SnackBarManager = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            viewModel.onAction(FeedAction.LocationPermissionResult(granted))
        }

    BackHandler {
        val currentTime = System.currentTimeMillis()

        if (currentTime - backPressedTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            backPressedTime = currentTime
        }
    }

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

                is FeedEffect.ShowError -> {
                    snackBarManager.showError(
                        message = effect.message,
                        actionLabel = "Dismiss",
                        onAction = { snackBarManager.dismiss() },
                    )
                }

                FeedEffect.GetLocation -> {
                    locationClient
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let {
                                viewModel.onAction(
                                    FeedAction.LocationReceived(
                                        it.latitude,
                                        it.longitude,
                                    ),
                                )
                            }
                        }
                }

                FeedEffect.RequestLocationPermission -> {
                    val permissions =
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    val allGranted =
                        permissions.all {
                            ContextCompat.checkSelfPermission(
                                context,
                                it,
                            ) == PackageManager.PERMISSION_GRANTED
                        }
                    if (allGranted) {
                        viewModel.onAction(FeedAction.LocationPermissionResult(true))
                    } else {
                        permissionLauncher.launch(permissions)
                    }
                }
            }
        }
    }

    LaunchedEffect(state.showFabTooltip) {
        if (state.showFabTooltip) {
            kotlinx.coroutines.delay(5000)
            viewModel.onAction(FeedAction.DismissFabTooltip)
        }
    }

    FeedScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToContribute = onNavigateToContribute,
        modifier = modifier,
    )
}

@Composable
internal fun FeedScreenContent(
    state: FeedState,
    onAction: (FeedAction) -> Unit,
    onNavigateToContribute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            MwenyejiLargeHeaderBar(
                title = stringResource(R.string.where_to),
                subtitle = stringResource(R.string.find_local_ways_to_move_around_nairobi),
                content = {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onAction(FeedAction.SearchQueryChanged(it)) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_area_stage_destination),
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TooltipWithAnimation(
                    show = state.showFabTooltip,
                    text = stringResource(R.string.know_a_route_add_it_here),
                    emoji = "",
                    onDismiss = { onAction(FeedAction.DismissFabTooltip) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                FloatingActionButton(
                    onClick = onNavigateToContribute,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
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
                    TextButton(onClick = { onAction(FeedAction.RetryClicked) }) {
                        Text(stringResource(R.string.retry), color = colors.primary)
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
                                        onAction(FeedAction.SelectTimeOfDay(timeOfDay))
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
                                onClick = { onAction(FeedAction.SeeAllClicked) },
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
                                onClick = { onAction(FeedAction.RouteClicked(route)) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TooltipWithAnimation(show: Boolean, text: String, emoji: String, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "TooltipBounce")
    val offsetY by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = (-8).dp,
        typeConverter = Dp.VectorConverter,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1200, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "bounce",
    )

    AnimatedVisibility(
        visible = show,
        enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + shrinkVertically() + scaleOut(targetScale = 0.8f),
    ) {
        MwenyejiTooltip(
            text = text,
            emoji = emoji,
            onDismiss = onDismiss,
            modifier = Modifier.offset(y = offsetY), // Apply the bounce here
            visible = show,
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
            onAction = {},
            onNavigateToContribute = {},
        )
    }
}

@Composable
fun TimeOfDayChip(title: String, modifier: Modifier = Modifier, selected: Boolean = false, onSelected: (String) -> Unit = {}) {
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
fun TimeOfDayChipGroup(options: List<String>, selectedOption: String?, onOptionSelected: (String) -> Unit, modifier: Modifier = Modifier) {
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
