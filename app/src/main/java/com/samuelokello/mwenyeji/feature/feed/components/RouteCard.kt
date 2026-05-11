package com.samuelokello.mwenyeji.feature.feed.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.data.models.BoardableRoute
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteConfidence
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TripDirection
import com.samuelokello.mwenyeji.presentation.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun RouteCard(boardableRoute: BoardableRoute, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val route = boardableRoute.route
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography
    val context = LocalContext.current

    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        border = BorderStroke(width = 1.dp, color = colors.border),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    route.routeNumber?.let { number ->
                        RouteTagChip(label = number, isPrimary = true)
                    }
                    Text(
                        text = boardableRoute.boardingStop.name,
                        style = typography.titleMedium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(text = "→", style = typography.titleMedium, color = colors.primary)
                    Text(
                        text = boardableRoute.onwardTerminus,
                        style = typography.titleMedium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ConfidenceDot(confidence = route.confidence)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = boardableRoute.getWalkingLabel(context),
                    style = typography.bodySmall,
                    color = colors.primary,
                )
                if (boardableRoute.stopsRemaining > 0) {
                    Text(
                        text = "·",
                        style = typography.bodySmall,
                        color = colors.outlineVariant,
                    )
                    Text(
                        text = stringResource(R.string.stops_remaining_format, boardableRoute.stopsRemaining),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                route.peakHeadwayMins?.let { headway ->
                    Text(
                        text = "·",
                        style = typography.bodySmall,
                        color = colors.outlineVariant,
                    )
                    Text(
                        text = stringResource(R.string.peak_headway_format, headway),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = route.getFormattedGuideCount(context),
                    style = typography.labelSmall,
                    color = if (route.hasGuides) colors.primary else colors.onSurfaceVariant,
                )
                if (route.confirmedCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(text = "✓", style = typography.labelSmall, color = colors.success)
                        val timeStr = route.lastConfirmedAt?.toRelativeTime(context) ?: stringResource(R.string.recently)
                        Text(
                            text = stringResource(R.string.confirmed_at_format, timeStr),
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

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

@Composable
internal fun RouteTagChip(label: String, modifier: Modifier = Modifier, isPrimary: Boolean = false) {
    val colors = MwenyejiTheme.colorScheme
    Box(
        modifier =
            modifier
                .clip(MwenyejiTheme.shapes.extraSmall)
                .background(if (isPrimary) colors.primaryContainer else colors.surfaceContainerHigh)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MwenyejiTheme.typography.labelSmall,
            color = if (isPrimary) colors.onPrimaryContainer else colors.onSurfaceVariant,
        )
    }
}

private fun Long.toRelativeTime(context: Context): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> context.getString(R.string.just_now)
        minutes < 60 -> context.getString(R.string.minutes_ago_short, minutes.toInt())
        hours < 24 -> context.getString(R.string.hours_ago_short, hours.toInt())
        else -> context.getString(R.string.days_ago_short, days.toInt())
    }
}

private fun Route.getFormattedGuideCount(context: Context): String {
    return when (guideCount) {
        0 -> context.getString(R.string.no_guides_yet)
        1 -> context.getString(R.string.one_guide)
        else -> context.getString(R.string.guides_count_format, guideCount)
    }
}

private fun BoardableRoute.getWalkingLabel(context: Context): String {
    return when {
        walkingDistanceMetres < 50 -> context.getString(R.string.right_here)
        walkingDistanceMetres < 200 -> context.getString(R.string.walking_distance_m_format, walkingDistanceMetres)
        else -> context.getString(R.string.walking_distance_km_format, (walkingDistanceKm * 10).toInt() / 10.0)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun RouteCardPreview() {
    MwenyejiAppTheme {
        RouteCard(
            boardableRoute =
                BoardableRoute(
                    route =
                        Route(
                            id = "preview",
                            routeNumber = "34J",
                            from = "Ambassadeur",
                            to = "JKIA",
                            stopCount = 18,
                            peakHeadwayMins = 5,
                            guideCount = 3,
                            confirmedCount = 47,
                            lastConfirmedAt = System.currentTimeMillis() - 7_200_000L,
                        ),
                    boardingStop =
                        RouteStop(
                            stopId = "preview_stop",
                            name = "Ambassadeur",
                            lat = -1.286,
                            lng = 36.826,
                            sequence = 1,
                        ),
                    walkingDistanceKm = 0.12,
                    onwardTerminus = "JKIA",
                    stopsRemaining = 18,
                    tripDirection = TripDirection.OUTBOUND,
                ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
