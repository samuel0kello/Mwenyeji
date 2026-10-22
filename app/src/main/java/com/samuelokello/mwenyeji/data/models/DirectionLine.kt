package com.samuelokello.mwenyeji.data.models

/**
 * One direction of a route as the route data stores it: its stops and the road it follows, both in
 * the direction of travel. A matatu into town stops on the into-town side of a dual carriageway, so
 * each direction has its own stops. Either list is empty when the data has none.
 */
data class DirectionLine(
    val stops: List<RouteStop>,
    val road: List<GeoPoint>,
)
