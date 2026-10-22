package com.samuelokello.mwenyeji.data.models

/** Which way along a route a trip runs: in the order its stops are stored, or the other way. */
enum class TravelDirection(
    val key: String,
) {
    OUTBOUND("outbound"),
    INBOUND("inbound"),
}

/**
 * The path a matatu took on one guided trip, recorded only while the rider was on board and only with
 * their consent. It carries no account, device or date, only the hour and weekday the ride started,
 * so peak-hour shortcuts can be told from off-peak ones.
 */
data class RideTrace(
    val routeId: String,
    val direction: TravelDirection,
    val boardingStopId: String,
    val alightStopId: String,
    val weekday: Int,
    val hour: Int,
    val points: List<TracePoint>,
)

/**
 * @param secondsFromStart Seconds since the first point of the ride.
 * @param speedMps         Speed in metres per second, as the phone reported it.
 */
data class TracePoint(
    val lat: Double,
    val lng: Double,
    val secondsFromStart: Int,
    val speedMps: Double,
)
