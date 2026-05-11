package com.samuelokello.mwenyeji.feature.feed.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.feature.feed.components.RouteCard
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiTopBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.pulltorefresh.MwenyejiPullToRefresh
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AllRoutes(
    onNavigateToRouteDetail: (id: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllRoutesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AllRoutesEffects.NavigateToRouteDetail -> {
                    onNavigateToRouteDetail(effect.route.id)
                }

                is AllRoutesEffects.ShowError -> {
                    Unit
                }
            }
        }
    }

    AllRoutesContent(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                delay(5_000L)
                isRefreshing = false
            }
        },
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun AllRoutesContent(
    state: AllRoutesState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onAction: (AllRoutesActions) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MwenyejiTopBar(title = "All routes", onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        MwenyejiPullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            confirmationText = "Updated • ${state.routes.size} routes",
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            val cardRotation = 5f * pullProgress.coerceAtMost(1f)
            val effectiveRotation = if (isRefreshing) 5f else cardRotation

            LazyColumn {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                indicatorItem()

                // routes is List<BoardableRoute> — key must be unique per (routeId, direction)
                itemsIndexed(
                    items = state.routes,
                    key = { _, br -> "${br.route.id}_${br.tripDirection}" },
                ) { index, boardableRoute ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .zIndex((state.routes.size - index).toFloat())
                                .graphicsLayer {
                                    rotationZ = effectiveRotation * if (index % 2 == 0) 1f else -1f
                                },
                    ) {
                        RouteCard(
                            boardableRoute = boardableRoute,
                            onClick = { onAction(AllRoutesActions.RouteClicked(boardableRoute)) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
