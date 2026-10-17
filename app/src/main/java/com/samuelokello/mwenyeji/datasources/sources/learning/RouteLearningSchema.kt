package com.samuelokello.mwenyeji.datasources.sources.learning

internal object RouteLearningSchema {
    // Written by riders' phones, read only by seeder/route_learning.
    const val RIDE_TRACES_COLLECTION = "ride_traces"

    // Written by seeder/route_learning, read by phones when a trip starts.
    const val ROUTE_VARIANTS_COLLECTION = "route_variants"

    object Fields {
        const val ROUTE_ID = "routeId"
        const val DIRECTION = "direction"
        const val CREATED_AT = "createdAt"
    }
}
