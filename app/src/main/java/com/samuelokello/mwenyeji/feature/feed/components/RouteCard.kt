package com.samuelokello.mwenyeji.feature.feed.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Route number badge — rounded square, not a pill
                route.routeNumber?.let { number ->
                    RouteNumberBadge(
                        number = number,
                        color = colors.primary,
                    )
                }

                // Route name + boarding subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = boardableRoute.boardingStop.name,
                            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "\u2192",
                            style = typography.titleMedium,
                            color = colors.onSurface,
                        )
                        Text(
                            text = boardableRoute.onwardTerminus,
                            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                    // subtitle
                    Text(
                        text = "Board at ${boardableRoute.boardingStop.name}",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }

                // Walk distance hero
                if (boardableRoute.walkingDistanceKm != Double.MAX_VALUE) {
                    WalkDistanceHero(
                        boardableRoute = boardableRoute,
                        primaryColor = colors.primary,
                        context = context,
                    )
                }
            }

            DashedDivider(color = colors.border)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Stops count
                if (boardableRoute.stopsRemaining > 0) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "${boardableRoute.stopsRemaining}",
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            text = "stops",
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = "\u00b7",
                    style = typography.bodyLarge,
                    color = colors.outlineVariant,
                )

                // Peak headway
                route.peakHeadwayMins?.let { headway ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "every ${headway}min",
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            text = "peak",
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }

                // Confidence dot + guide count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ConfidenceDot(confidence = route.confidence)
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (route.guideCount > 0) "${route.guideCount}" else "–",
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (route.hasGuides) colors.primary else colors.onSurfaceVariant,
                        )
                        Text(
                            text = "guides",
                            style = typography.labelSmall,
                            color = if (route.hasGuides) colors.primary else colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteNumberBadge(number: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number,
            style =
                MwenyejiTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                ),
            color = color,
        )
    }
}

@Composable
private fun WalkDistanceHero(boardableRoute: BoardableRoute, primaryColor: Color, context: Context, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        // "90m" — number large, unit smaller, inline
        val label = boardableRoute.getWalkingLabel(context)
        Text(
            text = label,
            style =
                MwenyejiTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                ),
            color = primaryColor,
        )
        Text(
            text = "WALK",
            style =
                MwenyejiTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp,
                ),
            color = primaryColor.copy(alpha = 0.7f),
        )
    }
}

// ── Dashed divider ────────────────────────────────────────────────────────────

@Composable
private fun DashedDivider(color: Color, modifier: Modifier = Modifier) {
    val dashColor = color
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .drawWithContent {
                    val dashWidth = 8.dp.toPx()
                    val gapWidth = 4.dp.toPx()
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height / 2
                    drawLine(
                        color = dashColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth), 0f),
                    )
                }.then(Modifier.size(width = 0.dp, height = 1.dp)),
    )
}

// ── Confidence dot ────────────────────────────────────────────────────────────

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
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
    )
}

// ── Helper extensions ─────────────────────────────────────────────────────────

internal fun BoardableRoute.getWalkingLabel(context: Context): String =
    when {
        walkingDistanceKm == Double.MAX_VALUE -> {
            ""
        }

        walkingDistanceMetres < 50 -> {
            context.getString(R.string.right_here)
        }

        walkingDistanceMetres < 200 -> {
            context.getString(
                R.string.walking_distance_m_format,
                walkingDistanceMetres,
            )
        }

        else -> {
            context.getString(
                R.string.walking_distance_km_format,
                (walkingDistanceKm * 10).toInt() / 10.0,
            )
        }
    }

internal fun Route.getFormattedGuideCount(context: Context): String =
    when (guideCount) {
        0 -> context.getString(R.string.no_guides_yet)
        1 -> context.getString(R.string.one_guide)
        else -> context.getString(R.string.guides_count_format, guideCount)
    }

// ── Tag chip (used by other screens) ─────────────────────────────────────────

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
            style = MwenyejiTheme.typography.labelMedium,
            color = if (isPrimary) colors.onPrimaryContainer else colors.onSurfaceVariant,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewRoute =
    BoardableRoute(
        route =
            Route(
                id = "preview",
                routeNumber = "46",
                from = "Kencom",
                to = "Kawangware",
                stopCount = 14,
                peakHeadwayMins = 5,
                guideCount = 2,
                confirmedCount = 23,
            ),
        boardingStop =
            RouteStop(
                stopId = "s1",
                name = "Kencom",
                lat = -1.286,
                lng = 36.826,
                sequence = 1,
            ),
        walkingDistanceKm = 0.09,
        onwardTerminus = "Kawangware",
        stopsRemaining = 14,
        tripDirection = TripDirection.OUTBOUND,
    )

@Preview(
    name = "Dark theme",
    showBackground = true,
    backgroundColor = 0xFF0E1210,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RouteCardDarkPreview() {
    MwenyejiAppTheme {
        RouteCard(
            boardableRoute = previewRoute,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "Light theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun RouteCardLightPreview() {
    MwenyejiAppTheme {
        RouteCard(
            boardableRoute = previewRoute,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "No walking distance (no location)",
    showBackground = true,
    backgroundColor = 0xFF0E1210,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RouteCardNoLocationPreview() {
    MwenyejiAppTheme {
        RouteCard(
            boardableRoute = previewRoute.copy(walkingDistanceKm = Double.MAX_VALUE),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
