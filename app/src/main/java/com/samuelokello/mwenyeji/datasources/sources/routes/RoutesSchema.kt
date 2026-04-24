package com.samuelokello.mwenyeji.datasources.sources.routes

internal object RoutesSchema {
    const val COLLECTION = "routes"
    const val CONFIRMATIONS_SUBCOLLECTION = "confirmations"

    object Fields {
        const val CONFIRMED_COUNT = "confirmedCount"
        const val DIDNT_WORK_COUNT = "didntWorkCount"
        const val OUTDATED_COUNT = "outdatedCount"
        const val BEST_TIME_OF_DAY = "bestTimeOfDay"
        const val LAST_CONFIRMED_AT = "lastConfirmedAt"
    }
}
