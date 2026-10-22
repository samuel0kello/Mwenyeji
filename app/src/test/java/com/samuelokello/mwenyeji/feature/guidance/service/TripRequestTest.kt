package com.samuelokello.mwenyeji.feature.guidance.service

import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TravelDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripRequestTest {
    // Five stops heading east, roughly 1.1km apart, as the route stores them.
    private val stops = List(5) { RouteStop("s$it", "Stop $it", -1.28, 36.82 + it * 0.01, it) }

    private fun request(
        reverse: Boolean = false,
        from: String? = null,
        to: String? = null,
    ) = TripRequest.forDirection("r1", stops, from, to, labelSaysReverse = reverse, vehicleName = "Super Metro", fare = 50)!!

    @Test
    fun `the trip ends at the searched destination`() {
        val plan = request(to = "stop 3").toPlan(hereLat = null, hereLng = null)

        assertEquals("Stop 3", plan.alightStop.name)
        assertEquals(50, plan.fare)
        assertEquals("Super Metro", plan.vehicleName)
    }

    @Test
    fun `without a matching name the trip ends at the far end of the route`() {
        val plan = request(to = "Somewhere else").toPlan(hereLat = null, hereLng = null)

        assertEquals("Stop 4", plan.alightStop.name)
    }

    @Test
    fun `the direction comes from where the stops sit, not from the route's label`() {
        // Boarding at stop 3 for stop 1 runs against the stored order, whatever the label says.
        val plan = request(from = "Stop 3", to = "Stop 1", reverse = false).toPlan(hereLat = null, hereLng = null)

        assertEquals(TravelDirection.INBOUND, request(from = "Stop 3", to = "Stop 1").direction)
        assertEquals("Stop 3", plan.stage.name)
        assertEquals("Stop 1", plan.alightStop.name)
        assertEquals(2, plan.stopsToRide)

        // And with the stored order even when the label says otherwise.
        assertEquals(TravelDirection.OUTBOUND, request(from = "Stop 1", to = "Stop 3", reverse = true).direction)
    }

    @Test
    fun `names match however they are written`() {
        val named = stops.mapIndexed { i, stop -> if (i == 1) stop.copy(name = "Mariguini") else stop }
        val request = TripRequest.forDirection("r1", named, "Marigu-ini", "stop  4", false, "Super Metro", null)!!
        val plan = request.toPlan(hereLat = null, hereLng = null)

        assertEquals("Mariguini", plan.stage.name)
        assertEquals("Stop 4", plan.alightStop.name)
    }

    @Test
    fun `the request knows its route and which way it runs`() {
        assertEquals("r1", request().routeId)
        assertEquals(TravelDirection.OUTBOUND, request().direction)
        assertEquals(TravelDirection.INBOUND, request(reverse = true).direction)
    }

    @Test
    fun `riding the other way reverses the stops`() {
        val plan = request(reverse = true).toPlan(hereLat = null, hereLng = null)

        assertEquals(stops.reversed(), plan.stops)
        assertEquals("Stop 4", plan.stage.name)
        assertEquals("Stop 0", plan.alightStop.name)
        assertEquals(4, plan.stopsToRide)
    }

    @Test
    fun `a searched origin that is a stop becomes the stage`() {
        val plan = request(from = "Stop 1").toPlan(hereLat = stops[3].lat, hereLng = stops[3].lng)

        assertEquals("Stop 1", plan.stage.name)
    }

    @Test
    fun `otherwise the stage is the stop nearest the user, measured to the nearest 10 metres`() {
        val here = stops[2]
        val plan = request().toPlan(hereLat = here.lat, hereLng = here.lng + 0.001)

        assertEquals("Stop 2", plan.stage.name)
        val walk = plan.walkingDistanceMetres!!
        assertEquals(0, walk % 10)
        assertTrue(walk in 100..120)
    }

    @Test
    fun `the stage is never the alight stop or past it`() {
        // Standing at the destination: board at the stop before it.
        val plan = request(to = "Stop 2").toPlan(hereLat = stops[4].lat, hereLng = stops[4].lng)

        assertEquals("Stop 1", plan.stage.name)
        assertEquals("Stop 2", plan.alightStop.name)
    }

    @Test
    fun `with no location the trip starts at the first stop and the walk is unknown`() {
        val plan = request().toPlan(hereLat = null, hereLng = null)

        assertEquals("Stop 0", plan.stage.name)
        assertNull(plan.walkingDistanceMetres)
    }

    @Test
    fun `fewer than two stops cannot make a trip`() {
        assertNull(TripRequest.forDirection("r1", stops.take(1), null, null, false, "Super Metro", null))
    }
}
