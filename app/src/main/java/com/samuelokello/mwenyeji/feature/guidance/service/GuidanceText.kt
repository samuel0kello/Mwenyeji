package com.samuelokello.mwenyeji.feature.guidance.service

import android.content.Context
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.guidance.session.TripPhase
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan

/** The one-line next step, shown in the notification and on the guidance screen. */
fun guidanceStatus(
    context: Context,
    plan: TripPlan?,
    phase: TripPhase,
): String {
    if (plan == null) return context.getString(R.string.guidance_status_starting)
    val stage = plan.stage.name
    val stop = plan.alightStop.name
    return when (phase) {
        TripPhase.PLANNED -> context.getString(R.string.guidance_status_starting)
        TripPhase.WALKING_TO_STAGE -> context.getString(R.string.guidance_status_walking, stage)
        TripPhase.OFF_ROUTE -> context.getString(R.string.guidance_status_off_route, stage)
        TripPhase.AT_STAGE -> context.getString(R.string.guidance_status_at_stage, stage, plan.vehicleName)
        TripPhase.ON_BOARD -> context.getString(R.string.guidance_status_on_board, stop)
        TripPhase.APPROACHING_STOP -> context.getString(R.string.guidance_status_approaching, stop)
        TripPhase.ALIGHT_NOW -> context.getString(R.string.guidance_status_alight, stop)
        TripPhase.MISSED_STOP -> context.getString(R.string.guidance_status_missed, stop)
        TripPhase.ARRIVED -> context.getString(R.string.guidance_status_arrived, stop)
        TripPhase.CANCELLED -> context.getString(R.string.guidance_status_cancelled)
    }
}
