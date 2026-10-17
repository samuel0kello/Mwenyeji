package com.samuelokello.mwenyeji.feature.planner.domain

import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TravelDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripPlannerTest {
    private val lat = -1.28
    private val metre = 1 / 111_320.0

    // Stops every ~1.1 km heading east.
    private fun line(
        id: String,
        count: Int,
        northMetres: Double = 0.0,
    ) = RouteLine(
        Route(id = id, routeNumber = id),
        List(count) { RouteStop("$id-s$it", "$id stop $it", lat + northMetres * metre, 36.80 + it * 0.01, it) },
    )

    private fun placeNear(
        stop: RouteStop,
        eastMetres: Double = 0.0,
        northMetres: Double = 0.0,
    ) = Place("near ${stop.name}", stop.lat + northMetres * metre, stop.lng + eastMetres * metre)

    @Test
    fun `boards at the stage nearest the start and gets off nearest the destination`() {
        val route = line("105", 6)
        val option = TripPlanner.plan(
            placeNear(route.stops[1], northMetres = 200.0),
            placeNear(route.stops[4], northMetres = 150.0),
            listOf(route),
        ).single()

        assertEquals(TravelDirection.OUTBOUND, option.direction)
        assertEquals("105 stop 1", option.boardingStop.name)
        assertEquals("105 stop 4", option.alightStop.name)
        assertEquals(3, option.stopsToRide)
        assertEquals(200, option.walkToStageMetres)
        assertEquals(150, option.walkFromStopMetres)
    }

    @Test
    fun `travelling against the stored order rides the route the other way`() {
        val route = line("105", 6)
        val option = TripPlanner.plan(placeNear(route.stops[4]), placeNear(route.stops[1]), listOf(route)).single()

        assertEquals(TravelDirection.INBOUND, option.direction)
        assertEquals("105 stop 4", option.boardingStop.name)
        assertEquals("105 stop 1", option.alightStop.name)
        assertEquals(route.stops.reversed(), option.stops)
    }

    @Test
    fun `a route too far to walk to is not offered`() {
        val near = line("105", 6)
        val far = line("33", 6, northMetres = 3_000.0)
        val options = TripPlanner.plan(placeNear(near.stops[0]), placeNear(near.stops[5]), listOf(near, far))

        assertEquals(listOf("105"), options.map { it.route.id })
    }

    @Test
    fun `options come least walking first`() {
        val close = line("105", 6, northMetres = 100.0)
        val further = line("111", 6, northMetres = 600.0)
        val from = placeNear(close.stops[0], northMetres = -100.0)
        val to = placeNear(close.stops[5], northMetres = -100.0)

        val options = TripPlanner.plan(from, to, listOf(further, close))

        assertEquals(listOf("105", "111"), options.map { it.route.id })
        assertTrue(options[0].walkToStageMetres < options[1].walkToStageMetres)
    }

    @Test
    fun `the trip request rides the planned stretch with the guide's sacco and fare`() {
        val route = line("105", 6)
        val option =
            TripPlanner
                .plan(placeNear(route.stops[1]), placeNear(route.stops[4]), listOf(route))
                .single()
                .copy(sacco = "Super Metro", fare = 80)

        val request = option.toTripRequest(defaultVehicle = "the route 105 matatu")
        val plan = request.toPlan(hereLat = null, hereLng = null)

        assertEquals("105 stop 1", plan.stage.name)
        assertEquals("105 stop 4", plan.alightStop.name)
        assertEquals("Super Metro", plan.vehicleName)
        assertEquals(80, plan.fare)
    }
}
