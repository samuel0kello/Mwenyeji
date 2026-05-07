package com.samuelokello.mwenyeji.datasources.sources.routes

internal object RoutesSchema {
    const val COLLECTION = "routes"
    const val ROUTE_STOPS_COLLECTION = "route_stops"
    const val STOPS_COLLECTION = "stops"
    const val CONFIRMATIONS_SUBCOLLECTION = "confirmations"

    object Fields {
        // Trust signals
        const val CONFIRMED_COUNT = "confirmedCount"
        const val DIDNT_WORK_COUNT = "didntWorkCount"
        const val OUTDATED_COUNT = "outdatedCount"
        const val LAST_CONFIRMED_AT = "lastConfirmedAt"

        // Community fields
        const val BEST_TIME_OF_DAY = "bestTimeOfDay"

        // GTFS fields
        const val SOURCE = "source"
        const val TERMINUS1_GEOHASH = "terminus1Geohash"
        const val TERMINUS2_GEOHASH = "terminus2Geohash"
        const val SEARCH_TERMS = "searchTerms"
        const val IS_ENRICHED = "isEnriched"
    }

    object Sources {
        const val COMMUNITY = "community"
        const val DIGITAL_MATATUS = "digital_matatus"
    }
}
