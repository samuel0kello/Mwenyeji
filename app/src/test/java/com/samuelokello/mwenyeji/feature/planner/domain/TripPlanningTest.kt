package com.samuelokello.mwenyeji.feature.planner.domain

import com.samuelokello.mwenyeji.core.location.CurrentLocation
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.models.Coordinates
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.data.repository.SearchRepository
import com.samuelokello.mwenyeji.feature.planner.data.RouteNetwork
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripPlanningTest {
    // One route east along a street, stops ~1.1 km apart: Odeon, Ngara, Pangani, Muthaiga.
    private val stops =
        listOf("Odeon", "Ngara", "Pangani", "Muthaiga").mapIndexed { i, name -> RouteStop("s$i", name, -1.28, 36.82 + i * 0.01, i) }

    private val routes =
        object : RoutesRepository {
            override fun observeRoutes(): Flow<DataResult<List<Route>>> =
                flowOf(DataResult.Success(listOf(Route(id = "r46", routeNumber = "46"))))

            override suspend fun getRouteStops(routeId: String): DataResult<List<RouteStop>> = DataResult.Success(stops)

            override fun observeGuides(routeId: String): Flow<DataResult<List<Guide>>> =
                flowOf(
                    DataResult.Success(
                        listOf(
                            Guide(id = "g1", sacco = "Embassava", fareKsh = 50.0, confirmedCount = 2),
                            Guide(id = "g2", sacco = "Super Metro", fareKsh = 80.0, confirmedCount = 9),
                        ),
                    ),
                )

            override fun observeRouteById(id: String): Flow<DataResult<Route?>> = flowOf(DataResult.Success(null))

            override suspend fun submitGuide(
                routeId: String,
                guide: Guide,
            ): DataResult<String> = error("not used")

            override suspend fun submitVerdict(
                routeId: String,
                userId: String,
                verdict: Verdict,
            ): DataResult<Unit> = error("not used")

            override suspend fun getUserVerdict(
                routeId: String,
                userId: String,
            ): DataResult<Verdict?> = error("not used")
        }

    private var searched = mutableListOf<String>()
    private val search =
        object : SearchRepository {
            override suspend fun search(request: SearchRequest): DataResult<List<SearchResult>> {
                searched += request.query
                return DataResult.Success(
                    if (request.query == "muthaiga mall") {
                        listOf(SearchResult("N1", "Muthaiga Mall", null, null, Coordinates(-1.2805, 36.8505)))
                    } else {
                        emptyList()
                    },
                )
            }

            override suspend fun select(id: String): DataResult<SearchResult> = error("not used")
        }

    private var here: Pair<Double, Double>? = -1.2805 to 36.8205
    private val location =
        object : CurrentLocation {
            override fun hasPermission() = here != null

            override suspend fun get() = here
        }

    private val planning = TripPlanning(RouteNetwork(routes), routes, search, location)

    @Test
    fun `a trip from here gives the matatu, the stage, the stop, and the guide's sacco and fare`() =
        runTest {
            val outcome = planning.plan("Take me to Muthaiga") as Outcome.Options

            assertEquals(TripPlanning.HERE, outcome.from.name)
            val option = outcome.options.single()
            assertEquals("Odeon", option.boardingStop.name)
            assertEquals("Muthaiga", option.alightStop.name)
            // The most confirmed guide wins.
            assertEquals("Super Metro", option.sacco)
            assertEquals(80, option.fare)
            assertTrue(searched.isEmpty())
        }

    @Test
    fun `a place we do not know is looked up online`() =
        runTest {
            val outcome = planning.plan("I want to go to Muthaiga Mall") as Outcome.Options

            assertEquals(listOf("muthaiga mall"), searched)
            assertEquals("Muthaiga", outcome.options.single().alightStop.name)
        }

    @Test
    fun `nowhere to be found is said plainly`() =
        runTest {
            assertEquals(Outcome.UnknownPlace("timbuktu"), planning.plan("take me to Timbuktu"))
        }

    @Test
    fun `from here without a location asks for it`() =
        runTest {
            here = null
            assertEquals(Outcome.NeedLocation, planning.plan("Take me to Muthaiga"))
        }

    @Test
    fun `from a named place does not need location`() =
        runTest {
            here = null
            val outcome = planning.plan("from Ngara to Muthaiga") as Outcome.Options
            assertEquals("Ngara", outcome.options.single().boardingStop.name)
        }

    @Test
    fun `a destination round the corner is a walk`() =
        runTest {
            val outcome = planning.plan("Odeon")
            assertTrue(outcome is Outcome.Walkable)
        }

    @Test
    fun `something that is not a trip`() =
        runTest {
            assertEquals(Outcome.NotATrip, planning.plan("what is the weather like in the city today"))
        }
}
