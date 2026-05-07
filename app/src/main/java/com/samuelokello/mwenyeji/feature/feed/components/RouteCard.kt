package com.samuelokello.mwenyeji.feature.feed.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteConfidence
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.presentation.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

/**
 * @param route    The route domain model to display.
 * @param onClick  Called when the card is tapped.
 * @param modifier Applied to the root [MwenyejiCard].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteCard(route: Route, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        border =
            BorderStroke(
                width = 1.dp,
                color = colors.border,
            ),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            //  Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = route.from,
                        style = typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "→",
                        style = typography.titleMedium,
                        color = colors.primary,
                    )
                    Text(
                        text = route.to,
                        style = typography.titleMedium,
                        color = colors.onSurface,
                    )
                }

                // Confidence dot
                ConfidenceDot(confidence = route.confidence)
            }

            // Via line
            Text(
                text = route.via,
                style = typography.bodySmall,
                color = colors.primary,
            )

            // Description — first step preview
            if (route.steps.isNotEmpty()) {
                Text(
                    text = route.steps.first().instruction,
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            //  Tags row — fare + route tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Fare chip
                route.formattedFare?.let { fare ->
                    RouteTagChip(label = fare)
                }

                // Route tags
                route.tags.forEach { tag ->
                    RouteTagChip(
                        label = tag.displayName,
                        isPrimary = tag == RouteTag.FAST || tag == RouteTag.CHEAP,
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 5. Meta row — confirmed timestamp · uses today
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "✓",
                    style = typography.labelSmall,
                    color = colors.success,
                )
                Text(
                    text = "Confirmed ${route.lastConfirmedAt?.toRelativeTime() ?: "recently"}",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                if (route.confirmedCount > 0) {
                    Text(
                        text = "·",
                        style = typography.labelSmall,
                        color = colors.outlineVariant,
                    )
                    Text(
                        text = "${route.confirmedCount} uses today",
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Coloured dot indicating route confidence level.
 * Green = HIGH, Amber = MEDIUM, Red = STALE, Grey = UNVERIFIED.
 */
@Composable
private fun ConfidenceDot(confidence: RouteConfidence, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    val color =
        when (confidence) {
            RouteConfidence.HIGH -> colors.success
            RouteConfidence.MEDIUM -> colors.warning
            RouteConfidence.STALE -> colors.error
            RouteConfidence.UNVERIFIED -> colors.outlineVariant
        }
    Box(
        modifier =
            modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
    )
}

/**
 * Small pill chip for route tags and fare.
 *
 * @param isPrimary When true uses a green-tinted surface matching
 *                  the "fast" / "Cheap" chips
 */
@Composable
internal fun RouteTagChip(label: String, modifier: Modifier = Modifier, isPrimary: Boolean = false) {
    val colors = MwenyejiTheme.colorScheme

    Box(
        modifier =
            modifier
                .clip(MwenyejiTheme.shapes.extraSmall)
                .background(
                    if (isPrimary) {
                        colors.primaryContainer
                    } else {
                        colors.surfaceContainerHigh
                    },
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MwenyejiTheme.typography.labelSmall,
            color =
                if (isPrimary) {
                    colors.onPrimaryContainer
                } else {
                    colors.onSurfaceVariant
                },
        )
    }
}

/**
 * Converts epoch millis to a SHORT relative time string.
 * e.g. 7_200_000L → "2h ago", 86_400_000L → "1d ago"
 *
 * For production use consider using [android.text.format.DateUtils.getRelativeTimeSpanString].
 */
private fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun RouteCardPreview() {
    MwenyejiAppTheme {
        RouteCard(
            route =
                Route(
                    from = "CBD",
                    to = "Westlands",
                    via = "via Uhuru Highway",
                    fareKsh = 50.0,
                    bestTimeOfDay = TimeOfDay.MORNING_RUSH,
                    steps =
                        listOf(
                            RouteStep(
                                order = 1,
                                instruction =
                                    "Board at Kencom, avoid Archives matatus during rush." +
                                        " Quick connection at Westlands roundabout.",
                            ),
                        ),
                    tags = setOf(RouteTag.FAST),
                    confirmedCount = 47,
                    lastConfirmedAt = System.currentTimeMillis() - 7_200_000L,
//                confidence = RouteConfidence.HIGH,
                ),
            onClick = {},
            modifier =
                Modifier
                    .padding(16.dp),
        )
    }
}
