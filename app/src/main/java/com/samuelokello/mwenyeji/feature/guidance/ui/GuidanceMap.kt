package com.samuelokello.mwenyeji.feature.guidance.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.core.map.MwenyejiMapStyle
import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan
import com.samuelokello.mwenyeji.presentation.designsystem.components.map.applyTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.MultiLineString
import org.maplibre.geojson.Point

/**
 * Mwenyeji's own map (see [MwenyejiMapStyle]), close in and tilted so buildings show their height,
 * following the rider. On top: the whole route faint, the roads matatus are known to take dashed, the
 * part this trip rides strong, the stops in between, and pins on the stage and the stop.
 */
@Composable
internal fun GuidanceMap(
    plan: TripPlan?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val colors = MwenyejiTheme.colorScheme
    val overlay =
        OverlayColors(
            route = colors.outline.toArgb(),
            ride = colors.primary.toArgb(),
            known = colors.secondary.toArgb(),
            stopFill = colors.surface.toArgb(),
        )

    val mapView =
        remember {
            MapLibre.getInstance(context)
            MapView(context).apply { onCreate(null) }
        }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    DisposableEffect(lifecycle, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // Leaving the screen while it is still showing: unwind the MapView ourselves.
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
        }
    }

    val currentPlan by rememberUpdatedState(plan)
    LaunchedEffect(mapView) {
        mapView.getMapAsync { loaded ->
            loaded.uiSettings.apply {
                // The attribution is drawn by the screen, where the buttons cannot cover it.
                isAttributionEnabled = false
                isLogoEnabled = false
            }
            // Open close in from the first frame, never on the whole world.
            loaded.cameraPosition = guidanceCamera(currentPlan?.stage?.let { LatLng(it.lat, it.lng) } ?: NAIROBI)
            loaded.setStyle(Style.Builder().fromJson(MwenyejiMapStyle.json(context))) { ready ->
                ready.applyTheme(colors)
                ready.addOverlay(context, overlay)
                loaded.followRider(context, ready)
                map = loaded
                style = ready
            }
        }
    }

    // The theme can change while the screen is open (dark mode at sunset).
    LaunchedEffect(style, colors) { style?.applyTheme(colors) }

    LaunchedEffect(map, style, plan) {
        val loaded = map ?: return@LaunchedEffect
        val ready = style ?: return@LaunchedEffect
        ready.showPlan(plan)
        // Following the rider only pans, it does not zoom. Until a fix arrives, show the stage.
        if (plan != null && loaded.locationComponent.lastKnownLocation == null) {
            loaded.cameraPosition = guidanceCamera(LatLng(plan.stage.lat, plan.stage.lng))
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private data class OverlayColors(
    val route: Int,
    val ride: Int,
    val known: Int,
    val stopFill: Int,
)

private fun Style.addOverlay(
    context: Context,
    colors: OverlayColors,
) {
    ContextCompat.getDrawable(context, R.drawable.ic_map_pin)?.let { addImage(PIN_IMAGE, it.toBitmap()) }

    listOf(ROUTE_SOURCE, KNOWN_SOURCE, RIDE_SOURCE, STOPS_SOURCE, PINS_SOURCE).forEach {
        addSource(GeoJsonSource(it, FeatureCollection.fromFeatures(emptyList())))
    }

    addLayer(
        LineLayer("$ROUTE_SOURCE-line", ROUTE_SOURCE).withProperties(
            PropertyFactory.lineColor(colors.route),
            PropertyFactory.lineWidth(3f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        ),
    )
    addLayer(
        LineLayer("$KNOWN_SOURCE-line", KNOWN_SOURCE).withProperties(
            PropertyFactory.lineColor(colors.known),
            PropertyFactory.lineWidth(4f),
            PropertyFactory.lineDasharray(arrayOf(1.5f, 1.5f)),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        ),
    )
    addLayer(
        LineLayer("$RIDE_SOURCE-line", RIDE_SOURCE).withProperties(
            PropertyFactory.lineColor(colors.ride),
            PropertyFactory.lineWidth(6f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        ),
    )
    addLayer(
        CircleLayer("$STOPS_SOURCE-circle", STOPS_SOURCE).withProperties(
            PropertyFactory.circleRadius(4f),
            PropertyFactory.circleColor(colors.stopFill),
            PropertyFactory.circleStrokeColor(colors.ride),
            PropertyFactory.circleStrokeWidth(2f),
        ),
    )
    addLayer(
        SymbolLayer("$PINS_SOURCE-symbol", PINS_SOURCE).withProperties(
            PropertyFactory.iconImage(PIN_IMAGE),
            PropertyFactory.iconSize(1.2f),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.textField(Expression.get(NAME)),
            PropertyFactory.textFont(MwenyejiMapStyle.FONT_BOLD),
            PropertyFactory.textSize(13f),
            PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
            PropertyFactory.textOffset(arrayOf(0f, 1.2f)),
            PropertyFactory.textColor(android.graphics.Color.BLACK),
            PropertyFactory.textHaloColor(android.graphics.Color.WHITE),
            PropertyFactory.textHaloWidth(1.5f),
        ),
    )
}

private fun Style.showPlan(plan: TripPlan?) {
    fun set(
        source: String,
        features: List<Feature>,
    ) = getSourceAs<GeoJsonSource>(source)?.setGeoJson(FeatureCollection.fromFeatures(features))

    if (plan == null) {
        listOf(ROUTE_SOURCE, KNOWN_SOURCE, RIDE_SOURCE, STOPS_SOURCE, PINS_SOURCE).forEach { set(it, emptyList()) }
        return
    }

    val points = plan.stops.map { it.point() }
    set(ROUTE_SOURCE, listOf(Feature.fromGeometry(LineString.fromLngLats(points))))
    set(
        KNOWN_SOURCE,
        plan.knownPaths
            .filter { it.size >= 2 }
            .takeIf { it.isNotEmpty() }
            ?.let { paths -> listOf(Feature.fromGeometry(MultiLineString.fromLngLats(paths.map { path -> path.map { it.point() } }))) }
            .orEmpty(),
    )
    set(RIDE_SOURCE, listOf(Feature.fromGeometry(LineString.fromLngLats(points.subList(plan.boardingIndex, plan.alightIndex + 1)))))
    set(STOPS_SOURCE, (plan.boardingIndex + 1 until plan.alightIndex).map { Feature.fromGeometry(points[it]) })
    set(
        PINS_SOURCE,
        listOf(plan.stage, plan.alightStop).map { stop ->
            Feature.fromGeometry(stop.point()).apply { addStringProperty(NAME, stop.name) }
        },
    )
}

/** The rider's dot, with the camera following it close in and tilted. Needs location permission. */
@SuppressLint("MissingPermission")
private fun MapLibreMap.followRider(
    context: Context,
    style: Style,
) {
    val granted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    if (!granted) return

    locationComponent.apply {
        activateLocationComponent(
            LocationComponentActivationOptions
                .builder(context, style)
                .useDefaultLocationEngine(true)
                .build(),
        )
        isLocationComponentEnabled = true
        renderMode = RenderMode.GPS
        // Jump to where the phone last was, close in, before tracking takes over.
        lastKnownLocation?.let { cameraPosition = guidanceCamera(LatLng(it.latitude, it.longitude)) }
        cameraMode = CameraMode.TRACKING_GPS
        zoomWhileTracking(GUIDANCE_ZOOM)
        tiltWhileTracking(GUIDANCE_TILT)
    }
}

private fun guidanceCamera(target: LatLng): CameraPosition =
    CameraPosition
        .Builder()
        .target(target)
        .zoom(GUIDANCE_ZOOM)
        .tilt(GUIDANCE_TILT)
        .build()

private fun RouteStop.point(): Point = Point.fromLngLat(lng, lat)

private fun GeoPoint.point(): Point = Point.fromLngLat(lng, lat)

// Where the camera waits when there is neither a plan nor a known location yet.
private val NAIROBI = LatLng(-1.2864, 36.8172)

// 3D buildings draw from about zoom 15, and tilting shows their height.
private const val GUIDANCE_ZOOM = 17.0
private const val GUIDANCE_TILT = 55.0

private const val ROUTE_SOURCE = "mw-route"
private const val KNOWN_SOURCE = "mw-known"
private const val RIDE_SOURCE = "mw-ride"
private const val STOPS_SOURCE = "mw-stops"
private const val PINS_SOURCE = "mw-pins"
private const val PIN_IMAGE = "mw-pin"
private const val NAME = "name"
