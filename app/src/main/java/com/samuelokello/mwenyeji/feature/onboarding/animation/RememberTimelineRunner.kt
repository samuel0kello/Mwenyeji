package com.samuelokello.mwenyeji.feature.onboarding.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Composable
fun <E> rememberTimelineRunner(
    isActive: Boolean,
    timeline: List<TimelineStep<E>>,
    onEvent: (E) -> Unit,
    onReset: () -> Unit,
) {
    LaunchedEffect(isActive) {
        if (!isActive) {
            onReset()
            return@LaunchedEffect
        }

        onReset()

        timeline.forEach { step ->
            delay(step.delayMillis)

            coroutineScope {
                step.actions
                    .map { action ->
                        launch {
                            action { onEvent(it) }
                        }
                    }.joinAll()
            }
        }
    }
}
