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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.feature.feed.components.RouteCard
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiEmptyState
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiLargeHeaderBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiSearchBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.pulltorefresh.MwenyejiPullToRefresh
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.MwenyejiTooltip
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@Suppress("EffectKeys")
@SuppressLint("MissingPermission")
@Composable
fun FeedScreen(
    onNavigateToRouteDetail: (String) -> Unit,
    onNavigateToSeeAll: () -> Unit,
    onNavigateToContribute: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel(),
    snackBarManager: SnackBarManager = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            viewModel.onAction(FeedAction.LocationPermissionResult(granted))
        }

    val backExitMessage = stringResource(R.string.press_back_again_to_exit)
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - backPressedTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            Toast.makeText(context, backExitMessage, Toast.LENGTH_SHORT).show()
            backPressedTime = now
        }
    }

    val dismissLabel = stringResource(R.string.dismiss)
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
                        actionLabel = dismissLabel,
                        onAction = { snackBarManager.dismiss() },
                    )
                }

                FeedEffect.GetLocation -> {
                    locationClient
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let {
                                viewModel.onAction(
                                    FeedAction.LocationReceived(it.latitude, it.longitude),
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
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
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
            viewModel.onAction(FeedAction.DismissFabTooltip)
        }
    }

    FeedScreenContent(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                delay(5_000L.milliseconds)
                isRefreshing = false
            }
        },
        onAction = viewModel::onAction,
        onNavigateToContribute = onNavigateToContribute,
        modifier = modifier,
    )
}

@Composable
internal fun FeedScreenContent(
    state: FeedState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MwenyejiSearchBar(
                            state = state.searchState,
                            placeholder = stringResource(R.string.search_area_stage_destination),
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(TimeOfDay.entries, key = { it.name }) { timeOfDay ->
                                TimeOfDayChip(
                                    title = timeOfDay.displayName,
                                    selected = state.selectedTimeOfDay == timeOfDay,
                                    onSelect = { onAction(FeedAction.SelectTimeOfDay(timeOfDay)) },
                                )
                            }
                        }
                    }
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
                ) { CircularProgressIndicator(color = colors.primary) }
            }

            state.error != null -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error.toString(),
                            style = typography.bodyMedium,
                            color = colors.error,
                        )
                        TextButton(onClick = { onAction(FeedAction.RetryClicked) }) {
                            Text(stringResource(R.string.retry), color = colors.primary)
                        }
                    }
                }
            }

            else -> {
                MwenyejiPullToRefresh(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    confirmationText = stringResource(R.string.updated_routes_format, state.filteredRoutes.size),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                ) {
                    val cardRotation = 5f * pullProgress.coerceAtMost(1f)
                    val effectiveRotation = if (isRefreshing) 5f else cardRotation

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        item(key = "section_header") {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val headerText =
                                    if (state.userLat != null) {
                                        stringResource(R.string.routes_near_you)
                                    } else {
                                        stringResource(R.string.all_routes)
                                    }
                                Text(
                                    text = headerText,
                                    style = typography.titleSmall,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = { onAction(FeedAction.SeeAllClicked) },
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.see_all),
                                        style = typography.labelMedium,
                                        color = colors.primary,
                                    )
                                }
                            }
                        }

                        // Refining proximity indicator
                        if (state.isRefiningProximity) {
                            item(key = "refining") {
                                Text(
                                    text = stringResource(R.string.finding_stops_near_you),
                                    style = typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                )
                            }
                        }

                        // Route cards
                        if (state.filteredRoutes.isEmpty() && state.searchQuery.isEmpty()) {
                            item(key = "empty_state") {
                                MwenyejiEmptyState(
                                    icon = Icons.Default.LocationOn,
                                    heading = stringResource(R.string.no_matatu_stages_near_you),
                                    body =
                                        stringResource(
                                            R.string.we_couldn_t_find_any_stops_within_500m,
                                        ),
                                    hintText = "Or move closer to a road · GPS accuracy ±15m",
                                )
                            }
                        } else {
                            itemsIndexed(
                                items = state.filteredRoutes,
                                key = { _, br -> "${br.route.id}_${br.tripDirection}" },
                            ) { index, boardableRoute ->
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .zIndex((state.filteredRoutes.size - index).toFloat())
                                            .graphicsLayer {
                                                rotationZ =
                                                    effectiveRotation * if (index % 2 == 0) 1f else -1f
                                            },
                                ) {
                                    RouteCard(
                                        boardableRoute = boardableRoute,
                                        onClick = { onAction(FeedAction.RouteClicked(boardableRoute)) },
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TooltipWithAnimation(show: Boolean, text: String, emoji: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
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
            modifier = Modifier.offset { IntOffset(y = offsetY.roundToPx(), x = 0) },
            visible = show,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun FeedScreenContentPreview() {
    MwenyejiAppTheme {
        FeedScreenContent(
            state = FeedState(selectedTimeOfDay = TimeOfDay.MORNING_RUSH),
            onAction = {},
            onNavigateToContribute = {},
            isRefreshing = false,
            onRefresh = {},
        )
    }
}

@Composable
fun TimeOfDayChip(title: String, modifier: Modifier = Modifier, selected: Boolean = false, onSelect: (String) -> Unit = {}) {
    val colors = MwenyejiTheme.colorScheme
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.outlineVariant,
        animationSpec = tween(150),
        label = "chipBorderColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurface,
        animationSpec = tween(350),
        label = "chipContentColor",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.secondaryContainer else colors.surface,
        animationSpec = tween(150),
        label = "chipContainerColor",
    )
    Surface(
        modifier =
            Modifier
                .clickable(
                    onClick = { onSelect(title) },
                ),
        color = containerColor,
        border =
            BorderStroke(
                width = if (selected) .8.dp else 0.1.dp,
                color = borderColor,
            ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .height(32.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = title, style = MwenyejiTheme.typography.labelMedium, color = contentColor)
        }
    }
}
