package com.samuelokello.mwenyeji.feature.feed.route

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.feature.feed.components.RouteCard
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiTopBar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AllRoutes(
    viewmodel: AllRoutesViewmodel = koinViewModel(),
    onNavigateToRouteDetail: (id: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewmodel.effects.collect { effects ->
            when(effects) {
                is AllRoutesEffects.NavigateToRouteDetail -> onNavigateToRouteDetail(effects.route.id)
                is AllRoutesEffects.ShowError -> {}
            }
        }
    }

    AllRoutesContent(
        state = state,
        onAction = viewmodel::onAction,
        onNavigateBack=onNavigateBack
    )

}


@Composable
fun AllRoutesContent(
    state: AllRoutesState,
    onAction: (AllRoutesActions) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MwenyejiTopBar(
                title = "",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(state.routes) { route ->
                RouteCard(
                    route = route,
                    onClick = { onAction(AllRoutesActions.RouteClicked(route)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}