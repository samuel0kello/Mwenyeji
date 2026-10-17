package com.samuelokello.mwenyeji.feature.planner.domain

/** A named point the user can travel from or to. */
data class Place(
    val name: String,
    val lat: Double,
    val lng: Double,
)
