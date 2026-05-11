package com.samuelokello.mwenyeji.datasources.sources.routes

internal object RoutesSchema {
    const val COLLECTION = "routes"
    const val ROUTE_STOPS_COLLECTION = "route_stops"
    const val STOPS_COLLECTION = "stops"

    // Subcollections under /routes/{routeId}
    const val GUIDES_SUBCOLLECTION = "guides"
    const val CONFIRMATIONS_SUBCOLLECTION = "confirmations"

    object Fields {
        // Route fields
        const val CONFIRMED_COUNT = "confirmedCount"
        const val GUIDE_COUNT = "guideCount"
        const val TERMINUS1_GEOHASH = "terminus1Geohash"
        const val TERMINUS2_GEOHASH = "terminus2Geohash"
        const val SEARCH_TERMS = "searchTerms"

        // Guide fields
        const val ROUTE_ID = "routeId"
        const val CONTRIBUTOR_ID = "contributorId"
        const val BEST_TIME_OF_DAY = "bestTimeOfDay"
        const val LAST_CONFIRMED_AT = "lastConfirmedAt"
        const val DIDNT_WORK_COUNT = "didntWorkCount"
        const val OUTDATED_COUNT = "outdatedCount"
    }
}
