package com.samuelokello.mwenyeji.core.map

import android.content.Context
import com.samuelokello.mwenyeji.BuildConfig

/**
 * Mwenyeji's own map style, bundled in assets/map with its icons and fonts. Only the tiles come over the
 * network: our PMTiles file when [BuildConfig.MAP_TILES_URL] is set, otherwise OpenFreeMap, which serves
 * the same OpenMapTiles schema so the style draws either one.
 */
object MwenyejiMapStyle {
    private const val STYLE_ASSET = "map/style.json"
    private const val TILES_PLACEHOLDER = "{{TILES_URL}}"
    private const val FALLBACK_TILES = "https://tiles.openfreemap.org/planet"

    /** OpenStreetMap's licence requires this to be visible wherever the map is. */
    const val ATTRIBUTION = "© OpenStreetMap contributors · © OpenMapTiles"

    // The font stacks the bundled glyphs cover. Text on our own layers must use one of them.
    val FONT_BOLD = arrayOf("Noto Sans Bold")

    fun json(context: Context): String =
        context.assets
            .open(STYLE_ASSET)
            .bufferedReader()
            .use { it.readText() }
            .replace(TILES_PLACEHOLDER, tilesSource())

    private fun tilesSource(): String = BuildConfig.MAP_TILES_URL.takeIf { it.isNotBlank() }?.let { "pmtiles://$it" } ?: FALLBACK_TILES
}
