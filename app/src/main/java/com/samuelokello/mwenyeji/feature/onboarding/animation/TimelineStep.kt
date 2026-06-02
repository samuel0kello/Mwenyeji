package com.samuelokello.mwenyeji.feature.onboarding.animation

import kotlinx.coroutines.delay

data class TimelineStep<E>(
    val delayMillis: Long,
    val actions: List<suspend (suspend (E) -> Unit) -> Unit>,
)

class TimelineBuilder<E> {
    private val steps = mutableListOf<TimelineStep<E>>()

    fun step(delayMillis: Long, action: suspend (emit: suspend (E) -> Unit) -> Unit) {
        steps += TimelineStep(delayMillis, listOf(action))
    }

    fun parallel(delayMillis: Long, vararg actions: suspend (emit: suspend (E) -> Unit) -> Unit) {
        steps += TimelineStep(delayMillis, actions.toList())
    }

    fun build(): List<TimelineStep<E>> = steps
}

fun <E> timeline(block: TimelineBuilder<E>.() -> Unit): List<TimelineStep<E>> = TimelineBuilder<E>().apply(block).build()

suspend fun <E> List<TimelineStep<E>>.play(emit: suspend (E) -> Unit) {
    forEach { step ->
        delay(step.delayMillis)
        step.actions.forEach { action -> action(emit) }
    }
}
