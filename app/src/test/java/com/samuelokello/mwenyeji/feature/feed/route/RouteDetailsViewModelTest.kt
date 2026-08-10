package com.samuelokello.mwenyeji.feature.feed.route

import com.samuelokello.mwenyeji.core.ml.GuideSuggestionEngine
import com.samuelokello.mwenyeji.core.network.ConnectivityObserver
import com.samuelokello.mwenyeji.core.network.ConnectivityStatus
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.feature.auth.AuthState
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: RouteDetailsViewModel
    private lateinit var fakeRouteRepository: FakeRoutesRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSnackBarManager: FakeSnackBarManager
    private lateinit var fakeSuggestionEngine: FakeGuideSuggestionEngine
    private lateinit var fakeConnectivityObserver: FakeConnectivityObserver

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRouteRepository = FakeRoutesRepository()
        fakeAuthRepository = FakeAuthRepository()
        fakeSnackBarManager = FakeSnackBarManager()
        fakeSuggestionEngine = FakeGuideSuggestionEngine()
        fakeConnectivityObserver = FakeConnectivityObserver()

        viewModel =
            RouteDetailsViewModel(
                routeRepository = fakeRouteRepository,
                authRepository = fakeAuthRepository,
                snackbarManager = fakeSnackBarManager,
                suggestionEngine = fakeSuggestionEngine,
                connectivityObserver = fakeConnectivityObserver,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRoute generates AI suggestion with multiple stops`() =
        runTest {
            // Given
            val routeId = "route-1"
            val route = Route(id = routeId, from = "A", to = "Z", firstStopId = "stop-a")
            val stops =
                listOf(
                    RouteStop("stop-a", "A", 0.0, 0.0, 1),
                    RouteStop("stop-b", "B", 0.1, 0.1, 2),
                    RouteStop("stop-c", "C", 0.2, 0.2, 3),
                    RouteStop("stop-z", "Z", 0.3, 0.3, 4),
                )
            fakeRouteRepository.route = route
            fakeRouteRepository.stops = stops
            fakeSuggestionEngine.suggestion = "Best at MIDDAY: Clear path. | fare≈50 KSh | tags: FAST"

            // When
            viewModel.onAction(RouteDetailsAction.LoadRoute(routeId))
            advanceUntilIdle()

            // Then
            val state = viewModel.state.value
            assertNotNull(state.suggestedGuide)
            val steps = state.suggestedGuide!!.steps

            // Should have: Board at A, Pass through B, Pass through C, Alight at Z
            assertEquals(4, steps.size)
            assertEquals("Board at A", steps[0].instruction)
            assertEquals("Pass through B", steps[1].instruction)
            assertEquals("Pass through C", steps[2].instruction)
            assertEquals("Alight at Z", steps[3].instruction)
        }

    @Test
    fun `loadRoute generates AI suggestion with no intermediate stops`() =
        runTest {
            // Given
            val routeId = "route-2"
            val route = Route(id = routeId, from = "A", to = "B", firstStopId = "stop-a")
            val stops =
                listOf(
                    RouteStop("stop-a", "A", 0.0, 0.0, 1),
                    RouteStop("stop-b", "B", 0.1, 0.1, 2),
                )
            fakeRouteRepository.route = route
            fakeRouteRepository.stops = stops
            fakeSuggestionEngine.suggestion = "Best at EVENING: Relaxed. | fare≈30 KSh | tags: CHEAP"

            // When
            viewModel.onAction(RouteDetailsAction.LoadRoute(routeId))
            advanceUntilIdle()

            // Then
            val state = viewModel.state.value
            assertNotNull(state.suggestedGuide)
            val steps = state.suggestedGuide!!.steps

            // Should have: Board at A, Tell conductor B, Alight at B
            assertEquals(3, steps.size)
            assertTrue(steps[1].instruction.contains("Tell conductor"))
        }
}

class FakeRoutesRepository : RoutesRepository {
    var route: Route? = null
    var stops: List<RouteStop> = emptyList()
    var guides: List<Guide> = emptyList()

    override fun observeRoutes(): Flow<DataResult<List<Route>>> = flowOf(DataResult.Success(emptyList()))

    override fun observeRouteById(id: String): Flow<DataResult<Route?>> = flowOf(DataResult.Success(route))

    override suspend fun getRouteStops(routeId: String): DataResult<List<RouteStop>> = DataResult.Success(stops)

    override fun observeGuides(routeId: String): Flow<DataResult<List<Guide>>> = flowOf(DataResult.Success(guides))

    override suspend fun submitGuide(
        routeId: String,
        guide: Guide,
    ): DataResult<String> = DataResult.Success("new-guide-id")

    override suspend fun submitVerdict(
        routeId: String,
        userId: String,
        verdict: Verdict,
    ): DataResult<Unit> = DataResult.Success(Unit)

    override suspend fun getUserVerdict(
        routeId: String,
        userId: String,
    ): DataResult<Verdict?> = DataResult.Success(null)
}

class FakeAuthRepository : AuthRepository {
    override val authState: Flow<AuthState> = flowOf(AuthState.SignedIn("test-user", null, null, null))
    override val currentUserId: String = "test-user"
    override val isAnonymous: Boolean = false

    override suspend fun signInAnonymously(): DataResult<String> = DataResult.Success("test-user")

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): DataResult<String> = DataResult.Success("test-user")

    override suspend fun createAccountWithEmail(
        email: String,
        password: String,
    ): DataResult<String> = DataResult.Success("test-user")

    override suspend fun signInWithGoogle(context: android.content.Context): DataResult<String> = DataResult.Success("test-user")

    override suspend fun signOut(context: android.content.Context): DataResult<Unit> = DataResult.Success(Unit)
}

class FakeSnackBarManager : SnackBarManager {
    private val _currentMessage = MutableStateFlow<SnackBarMessage?>(null)
    override val currentMessage: StateFlow<SnackBarMessage?> = _currentMessage.asStateFlow()

    override fun showSuccess(message: String) {}

    override fun showError(
        message: String,
        actionLabel: String?,
        onAction: (() -> Unit)?,
    ) {
    }

    override fun showInfo(
        message: String,
        actionLabel: String?,
        onAction: (() -> Unit)?,
    ) {
    }

    override fun dismiss() {}
}

class FakeGuideSuggestionEngine : GuideSuggestionEngine {
    var suggestion: String? = null

    override fun suggestGuide(
        stopId: String,
        arrivalMinutes: Float,
        stopSequence: Float,
    ): String? = suggestion
}

class FakeConnectivityObserver : ConnectivityObserver {
    private val _status = MutableStateFlow(ConnectivityStatus.Available)
    val status: StateFlow<ConnectivityStatus> = _status.asStateFlow()

    override fun observe(): Flow<ConnectivityStatus> = _status

    fun setStatus(status: ConnectivityStatus) {
        _status.value = status
    }
}
