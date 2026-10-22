package com.samuelokello.mwenyeji.feature.guidance.session

/** Eight-point compass, for "the stage is 200 metres north-east". */
enum class CompassDirection {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST,
    ;

    companion object {
        /** @param bearing Degrees clockwise from north, any value. */
        fun fromBearing(bearing: Double): CompassDirection {
            val normalised = ((bearing % 360) + 360) % 360
            return entries[((normalised + 22.5) / 45).toInt() % entries.size]
        }
    }
}
