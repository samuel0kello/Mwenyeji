package com.samuelokello.mwenyeji.presentation.designsystem.components.map

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.core.map.MwenyejiMapStyle
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

/** A pin the user can drag to correct a place's exact position. */
data class MapPin(
    val id: String,
    val lat: Double,
    val lng: Double,
)

/**
 * Mwenyeji's map with draggable pins, optionally joined by a line. Press a pin and move it; the new
 * position is reported once, when the finger lifts. The camera moves only when a pin is out of view,
 * so a drag does not jump the map.
 */
@Composable
fun MwenyejiPinMap(
    pins: List<MapPin>,
    onPinMoved: (id: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier,
    zoom: Double = 15.0,
    connectPins: Boolean = false,
) {
    val context = LocalContext.current
    val colors = MwenyejiTheme.colorScheme
    val lineColor = colors.primary.toArgb()
    val mapView = rememberMapViewWithLifecycle()
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    val currentPins by rememberUpdatedState(pins)
    val currentOnPinMoved by rememberUpdatedState(onPinMoved)
    val currentConnect by rememberUpdatedState(connectPins)

    LaunchedEffect(mapView) {
        mapView.getMapAsync { loaded ->
            loaded.uiSettings.apply {
                isAttributionEnabled = false
                isLogoEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            currentPins.centre()?.let { loaded.cameraPosition = cameraAt(it, zoom) }
            loaded.setStyle(Style.Builder().fromJson(MwenyejiMapStyle.json(context))) { ready ->
                ready.applyTheme(colors)
                ContextCompat.getDrawable(context, R.drawable.ic_map_pin)?.let { ready.addImage(PIN_IMAGE, it.toBitmap()) }
                ready.addSource(GeoJsonSource(LINE_SOURCE))
                ready.addSource(GeoJsonSource(PINS_SOURCE))
                ready.addLayer(
                    LineLayer(LINE_LAYER, LINE_SOURCE).withProperties(
                        PropertyFactory.lineColor(lineColor),
                        PropertyFactory.lineWidth(3f),
                        PropertyFactory.lineDasharray(arrayOf(2f, 1.5f)),
                    ),
                )
                ready.addLayer(
                    SymbolLayer(PINS_LAYER, PINS_SOURCE).withProperties(
                        PropertyFactory.iconImage(PIN_IMAGE),
                        PropertyFactory.iconSize(1.2f),
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                        PropertyFactory.iconAllowOverlap(true),
                    ),
                )
                mapView.dragPins(loaded, ready, pins = { currentPins }, connect = { currentConnect }) { id, at ->
                    currentOnPinMoved(id, at.latitude, at.longitude)
                }
                map = loaded
                style = ready
            }
        }
    }

    LaunchedEffect(style, colors) { style?.applyTheme(colors) }

    LaunchedEffect(map, style, pins, connectPins) {
        val loaded = map ?: return@LaunchedEffect
        val ready = style ?: return@LaunchedEffect
        ready.showPins(pins, connectPins)
        val bounds = loaded.projection.visibleRegion.latLngBounds
        if (pins.any { !bounds.contains(LatLng(it.lat, it.lng)) }) {
            pins.centre()?.let { loaded.animateCamera(CameraUpdateFactory.newLatLngZoom(it, zoom)) }
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        Text(
            text = MwenyejiMapStyle.ATTRIBUTION,
            style = MwenyejiTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .background(colors.surface.copy(alpha = 0.8f), MwenyejiTheme.shapes.small)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}

private fun Style.showPins(
    pins: List<MapPin>,
    connect: Boolean,
) {
    getSourceAs<GeoJsonSource>(PINS_SOURCE)?.setGeoJson(
        FeatureCollection.fromFeatures(
            pins.map { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply { addStringProperty(PIN_ID, it.id) } },
        ),
    )
    val line = if (connect &&
        pins.size >= 2
    ) {
        listOf(Feature.fromGeometry(LineString.fromLngLats(pins.map { Point.fromLngLat(it.lng, it.lat) })))
    } else {
        emptyList()
    }
    getSourceAs<GeoJsonSource>(LINE_SOURCE)?.setGeoJson(FeatureCollection.fromFeatures(line))
}

/**
 * MapLibre has no draggable pins without its outdated annotation plugin, so: a press on a pin takes the
 * gesture away from the map, moves the pin under the finger, and reports where it was let go.
 */
@SuppressLint("ClickableViewAccessibility")
private fun org.maplibre.android.maps.MapView.dragPins(
    map: MapLibreMap,
    style: Style,
    pins: () -> List<MapPin>,
    connect: () -> Boolean,
    onMoved: (String, LatLng) -> Unit,
) {
    var draggingId: String? = null
    setOnTouchListener { _, event ->
        val point = android.graphics.PointF(event.x, event.y)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                draggingId =
                    map
                        .queryRenderedFeatures(point, PINS_LAYER)
                        .firstOrNull()
                        ?.getStringProperty(PIN_ID)
                draggingId != null
            }

            MotionEvent.ACTION_MOVE -> {
                val id = draggingId ?: return@setOnTouchListener false
                val at = map.projection.fromScreenLocation(point)
                val moved = pins().map { if (it.id == id) it.copy(lat = at.latitude, lng = at.longitude) else it }
                style.showPins(moved, connect())
                true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val id = draggingId ?: return@setOnTouchListener false
                draggingId = null
                onMoved(id, map.projection.fromScreenLocation(point))
                true
            }

            else -> draggingId != null
        }
    }
}

private fun List<MapPin>.centre(): LatLng? =
    takeIf { it.isNotEmpty() }?.let { pins -> LatLng(pins.map { it.lat }.average(), pins.map { it.lng }.average()) }

private fun cameraAt(
    target: LatLng,
    zoom: Double,
) = org.maplibre.android.camera.CameraPosition
    .Builder()
    .target(target)
    .zoom(zoom)
    .build()

private const val PINS_SOURCE = "mw-pins"
private const val PINS_LAYER = "mw-pins-symbol"
private const val LINE_SOURCE = "mw-pin-line"
private const val LINE_LAYER = "mw-pin-line-layer"
private const val PIN_IMAGE = "mw-pin"
private const val PIN_ID = "id"
