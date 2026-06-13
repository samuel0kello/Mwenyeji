package com.samuelokello.mwenyeji.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.launch

fun Modifier.shimmerEffect(): Modifier = this.then(ShimmerElement)

private object ShimmerElement : ModifierNodeElement<ShimmerNode>() {
    override fun create(): ShimmerNode = ShimmerNode()

    override fun update(node: ShimmerNode) {
        // No mutable configuration variables to reconcile across updates
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmerEffect"
    }

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = 31
}

private class ShimmerNode :
    Modifier.Node(),
    DrawModifierNode {
    private val translation = Animatable(0f)

    private val shimmerColors =
        listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

    override fun onAttach() {
        super.onAttach()
        coroutineScope.launch {
            translation.animateTo(
                targetValue = 2000f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
            )
        }
    }

    override fun ContentDrawScope.draw() {
        val currentOffset = translation.value

        // Render linear layout shader without runtime allocations inside drawing frames
        drawRect(
            brush =
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(x = currentOffset - 1000f, y = currentOffset - 1000f),
                    end = Offset(x = currentOffset, y = currentOffset),
                ),
        )

        drawContent()
    }
}
