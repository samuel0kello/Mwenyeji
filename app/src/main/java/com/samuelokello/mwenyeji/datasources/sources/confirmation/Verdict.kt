package com.samuelokello.mwenyeji.datasources.sources.confirmation

import com.samuelokello.mwenyeji.datasources.sources.routes.RoutesSchema

enum class Verdict(
    val wireValue: String,
    val countField: String,
) {
    CONFIRMED("CONFIRMED", RoutesSchema.Fields.CONFIRMED_COUNT),
    DIDNT_WORK("DIDNT_WORK", RoutesSchema.Fields.DIDNT_WORK_COUNT),
    OUTDATED("OUTDATED", RoutesSchema.Fields.OUTDATED_COUNT),
    ;

    companion object {
        fun fromWire(value: String?): Verdict? = entries.firstOrNull { it.wireValue == value }
    }
}
