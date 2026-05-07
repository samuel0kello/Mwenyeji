package com.samuelokello.mwenyeji.data.models

/**
 * A single stage on a GTFS-sourced route.
 * Loaded on demand from /route_stops — not embedded in the Route domain model.
 */
data class RouteStop(
    val stopId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val sequence: Int,
)
