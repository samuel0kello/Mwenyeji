package com.samuelokello.mwenyeji.data.models

/**
 * A GTFS route enriched with the user's boarding context.
 *
 * The feed never shows raw Route objects — it shows BoardableRoute,
 * which answers the user's actual question: "I am here. What can I board,
 * where do I walk to, and where will it take me?"
 *
 * Created fresh on each location update. Not persisted.
 *
 * @param route             The underlying GTFS route data.
 * @param boardingStop      The specific stop the user walks to board.
 * @param walkingDistanceKm How far the user walks from their current location.
 * @param onwardTerminus    The name of the route's terminus from the boarding point.
 * @param stopsRemaining    Stops between the boarding stop and the terminus.
 *                          Gives a rough sense of journey length.
 * @param tripDirection     "outbound" or "inbound" — which direction of the
 *                          bidirectional GTFS route goes away from the user.
 */
data class BoardableRoute(
    val route: Route,
    val boardingStop: RouteStop,
    val walkingDistanceKm: Double,
    val onwardTerminus: String,
    val stopsRemaining: Int,
    val tripDirection: TripDirection,
) {
    val walkingDistanceMetres: Int
        get() = (walkingDistanceKm * 1000).toInt()

    val walkingLabel: String
        get() =
            when {
                walkingDistanceMetres < 50 -> "Right here"
                walkingDistanceMetres < 200 -> "${walkingDistanceMetres}m walk"
                else -> "${(walkingDistanceKm * 10).toInt() / 10.0}km walk"
            }

    /** What the route card shows as the destination from the user's perspective. */
    val displayDestination: String
        get() = onwardTerminus

    /** What the route card shows as the boarding point. */
    val displayBoardingPoint: String
        get() = boardingStop.name
}

enum class TripDirection {
    OUTBOUND, // dir=0 in GTFS — travelling away from user towards terminus2
    INBOUND, // dir=1 in GTFS — travelling away from user towards terminus1
}
