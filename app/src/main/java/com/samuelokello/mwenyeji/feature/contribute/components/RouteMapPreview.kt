package com.samuelokello.mwenyeji.feature.contribute.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@OptIn(MapboxExperimental::class)
@Composable
fun RouteMapPreview(
    fromLat: Double,
    fromLng: Double,
    fromDisplayName: String,
    toLat: Double? = null,
    toLng: Double? = null,
    toDisplayName: String? = null,
    onFromPinDragged: (Double, Double) -> Unit,
    onToPinDragged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    val fromPoint = Point.fromLngLat(fromLng, fromLat)
    val toPoint =
        if (toLat != null && toLng != null) {
            Point.fromLngLat(toLng, toLat)
        } else {
            null
        }

    // Camera centers on midpoint if both points exist, otherwise just from
    val cameraCenter =
        if (toPoint != null) {
            Point.fromLngLat(
                (fromLng + toLng!!) / 2,
                (fromLat + toLat!!) / 2,
            )
        } else {
            fromPoint
        }

    val cameraZoom = if (toPoint != null) 13.0 else 15.0

    val mapViewportState =
        key(fromLat, fromLng, toLat, toLng) {
            rememberMapViewportState {
                setCameraOptions {
                    center(cameraCenter)
                    zoom(cameraZoom)
                    pitch(0.0)
                }
            }
        }

    val fromMarker =
        rememberIconImage(
            key = "from-pin",
            painter = painterResource(id = R.drawable.ic_map_pin),
        )
    val toMarker =
        rememberIconImage(
            key = "to-pin",
            painter = painterResource(id = R.drawable.ic_map_pin),
        )

    Column(modifier = modifier.fillMaxWidth()) {
        MapboxMap(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = "mapbox://styles/mapbox/dark-v11") },
            scaleBar = {},
            compass = {},
            logo = {},
        ) {
            // From pin — always shown
            PointAnnotation(point = fromPoint) {
                iconImage = fromMarker
                iconSize = 1.2
                interactionsState.isDraggable = true
                interactionsState.onDragged { annotation ->
                    onFromPinDragged(
                        annotation.point.latitude(),
                        annotation.point.longitude(),
                    )
                }
            }

            // To pin — only shown when To is confirmed
            if (toPoint != null) {
                PointAnnotation(point = toPoint) {
                    iconImage = toMarker
                    iconSize = 1.2
                    interactionsState.isDraggable = true
                    interactionsState.onDragged { annotation ->
                        onToPinDragged(
                            annotation.point.latitude(),
                            annotation.point.longitude(),
                        )
                    }
                }

                // Line connecting from → to
                PolylineAnnotation(
                    points = listOf(fromPoint, toPoint),
                ) {
                    lineColor = colors.onPrimary
                    lineWidth = 3.0
                    // lineDash = listOf(2.0, 1.0) // dashed line
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "FROM: $fromDisplayName",
                style = typography.labelSmall,
                color = colors.primary,
                modifier = Modifier.weight(1f),
            )
            if (toDisplayName != null) {
                Text(
                    text = "TO: $toDisplayName",
                    style = typography.labelSmall,
                    color = colors.error,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Drag pins to adjust exact locations",
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}
