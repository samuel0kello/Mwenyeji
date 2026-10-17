package com.samuelokello.mwenyeji.feature.guidance.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.guidance.session.TripPhase
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan

/** The ongoing notification that shows the next step and the buttons a rider needs. */
internal object GuidanceNotifications {
    const val NOTIFICATION_ID = 4_201
    private const val CHANNEL_ID = "trip_guidance"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.guidance_channel_name),
            // The voice and haptics already alert the user; the notification stays quiet.
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = context.getString(R.string.guidance_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun starting(context: Context): Notification =
        base(context)
            .setContentTitle(context.getString(R.string.guidance_title))
            .setContentText(context.getString(R.string.guidance_status_starting))
            .addAction(0, context.getString(R.string.guidance_action_stop), action(context, TripGuidanceService.ACTION_CANCEL))
            .build()

    fun forPhase(
        context: Context,
        plan: TripPlan,
        phase: TripPhase,
    ): Notification {
        val text = guidanceStatus(context, plan, phase)
        val builder = base(context)
            .setContentTitle(context.getString(R.string.guidance_notification_title, plan.alightStop.name))
            .setContentText(text)

        when {
            phase == TripPhase.AT_STAGE ->
                builder.addAction(
                    0,
                    context.getString(R.string.guidance_action_on_board),
                    action(context, TripGuidanceService.ACTION_ON_BOARD),
                )

            phase.isRiding || phase == TripPhase.MISSED_STOP ->
                builder.addAction(
                    0,
                    context.getString(R.string.guidance_action_alighted),
                    action(context, TripGuidanceService.ACTION_ALIGHTED),
                )
        }
        if (!phase.isFinal) {
            builder.addAction(0, context.getString(R.string.guidance_action_stop), action(context, TripGuidanceService.ACTION_CANCEL))
        }
        return builder.build()
    }

    private fun base(context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_navigation)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openApp(context))

    private fun openApp(context: Context): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

    private fun action(
        context: Context,
        action: String,
    ): PendingIntent =
        PendingIntent.getService(
            context,
            action.hashCode(),
            Intent(context, TripGuidanceService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
