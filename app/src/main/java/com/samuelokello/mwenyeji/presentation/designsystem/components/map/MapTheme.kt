package com.samuelokello.mwenyeji.presentation.designsystem.components.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.samuelokello.mwenyeji.presentation.ui.theme.color.MwenyejiColorScheme
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer

/**
 * The map's colours, taken from the app theme so the map sits inside the app in light and dark.
 * Land is a surface, roads are the surface the theme puts on top of it, water and parks are the
 * theme's info and success hues mixed into the land so they stay quiet. POI icons keep their own
 * colours: they are recognised by colour.
 */
private class MapPalette(
    colors: MwenyejiColorScheme,
) {
    private val dark = colors.background.luminance() < 0.5f

    val land = colors.surfaceContainerLow
    val landuse = colors.surfaceContainer
    val green = lerp(land, colors.success, if (dark) 0.22f else 0.18f)
    val water = lerp(land, colors.info, if (dark) 0.35f else 0.3f)
    val building = colors.surfaceContainerHigh
    val buildingTop = colors.surfaceContainerHighest
    val road = if (dark) colors.surfaceContainerHighest else colors.surfaceContainerLowest
    val majorRoad = lerp(road, colors.primaryContainer, 0.45f)
    val roadEdge = colors.outlineVariant
    val boundary = colors.outline
    val label = colors.onSurfaceVariant
    val placeLabel = colors.onSurface
    val waterLabel = lerp(colors.info, colors.onSurface, 0.35f)
    val halo = land
}

/** Recolours the bundled style's layers. Layers whose id starts with "mw-" are ours and left alone. */
fun Style.applyTheme(colors: MwenyejiColorScheme) {
    val palette = MapPalette(colors)
    layers.filterNot { it.id.startsWith("mw-") }.forEach { layer ->
        when (layer) {
            is BackgroundLayer -> layer.setProperties(PropertyFactory.backgroundColor(palette.land.argb))

            is FillLayer -> fillColor(layer.sourceLayer, layer.id, palette)?.let {
                layer.setProperties(PropertyFactory.fillColor(it.argb), PropertyFactory.fillOutlineColor(it.argb))
            }

            is FillExtrusionLayer -> layer.setProperties(PropertyFactory.fillExtrusionColor(palette.buildingTop.argb))

            is LineLayer -> lineColor(layer.sourceLayer, layer.id, palette)?.let {
                layer.setProperties(PropertyFactory.lineColor(it.argb))
            }

            is SymbolLayer -> {
                // Shields draw their text on their own badge image.
                if (layer.id.contains("shield")) return@forEach
                val text =
                    when (layer.sourceLayer) {
                        "place" -> palette.placeLabel
                        "water_name", "waterway" -> palette.waterLabel
                        else -> palette.label
                    }
                layer.setProperties(PropertyFactory.textColor(text.argb), PropertyFactory.textHaloColor(palette.halo.argb))
            }
        }
    }
}

private fun fillColor(
    sourceLayer: String,
    id: String,
    palette: MapPalette,
): Color? =
    when (sourceLayer) {
        "water" -> palette.water

        "park" -> palette.green

        "landcover" -> if (id.contains("wood") || id.contains("grass") || id.contains("wetland")) palette.green else palette.landuse

        "landuse" -> if (id.contains("pitch")) palette.green else palette.landuse

        "building" -> palette.building

        "aeroway" -> palette.landuse

        // The road-area fill is a pattern; colouring it would hide the pattern.
        else -> null
    }

private fun lineColor(
    sourceLayer: String,
    id: String,
    palette: MapPalette,
): Color? =
    when (sourceLayer) {
        "waterway" -> palette.water

        "park" -> palette.green

        "boundary" -> palette.boundary

        "aeroway" -> palette.road

        "transportation" ->
            when {
                id.contains("casing") -> palette.roadEdge

                // Railways and paths are drawn as dashes in the edge colour, so they read as different.
                id.contains("rail") || id.contains("path") || id.contains("pedestrian") -> palette.roadEdge

                MAJOR_ROADS.any { id.contains(it) } -> palette.majorRoad

                else -> palette.road
            }

        else -> null
    }

private val MAJOR_ROADS = listOf("motorway", "trunk", "primary")

private val Color.argb: Int get() = toArgb()
