package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.Message

/**
 * How a sentence enters the speech queue.
 *
 * Urgent sentences ask the user to act now, or warn that the app can no longer help them.
 * They cut off a normal sentence, but never another urgent one.
 */
enum class SpeechPriority {
    NORMAL,
    URGENT,
}

val Message.priority: SpeechPriority
    get() = when (this) {
        is Message.WalkToStage -> SpeechPriority.NORMAL

        is Message.WalkProgress -> SpeechPriority.NORMAL

        is Message.AtStage -> SpeechPriority.NORMAL

        Message.WalkingAwayFromStage -> SpeechPriority.NORMAL

        is Message.OnBoardConfirmed -> SpeechPriority.NORMAL

        is Message.GetReady -> SpeechPriority.NORMAL

        // Reassurance only: the user has nothing to do.
        is Message.Shortcut -> SpeechPriority.NORMAL

        is Message.WrongBus -> SpeechPriority.URGENT

        is Message.Alight -> SpeechPriority.URGENT

        // Nothing left to do, and urgent would cut off the end of "Alight".
        is Message.Arrived -> SpeechPriority.NORMAL

        Message.MissedStop -> SpeechPriority.URGENT

        is Message.OffBus -> SpeechPriority.NORMAL

        // Without GPS the app cannot say when to get off, so the user must watch for the stop.
        Message.LostLocation -> SpeechPriority.URGENT
    }
