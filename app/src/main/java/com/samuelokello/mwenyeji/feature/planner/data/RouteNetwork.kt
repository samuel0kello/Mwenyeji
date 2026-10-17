package com.samuelokello.mwenyeji.feature.planner.data

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.feature.planner.domain.Place
import com.samuelokello.mwenyeji.feature.planner.domain.PlaceDirectory
import com.samuelokello.mwenyeji.feature.planner.domain.RouteLine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Every route with its stops, loaded once per app run. Firestore keeps its own copy on the phone, so
 * after the first load this works with no network.
 */
class RouteNetwork(
    private val routes: RoutesRepository,
) {
    data class Snapshot(
        val lines: List<RouteLine>,
        val places: PlaceDirectory,
    )

    private val lock = Mutex()
    private var cached: Snapshot? = null

    /** Null when no routes could be loaded at all. */
    suspend fun load(): Snapshot? =
        lock.withLock {
            cached ?: fetch()?.also { cached = it }
        }

    private suspend fun fetch(): Snapshot? =
        coroutineScope {
            val all = (routes.observeRoutes().first() as? DataResult.Success)?.data.orEmpty()
            val lines =
                all
                    .chunked(BATCH)
                    .flatMap { batch ->
                        batch
                            .map { route ->
                                async {
                                    (routes.getRouteStops(route.id) as? DataResult.Success)
                                        ?.data
                                        ?.takeIf { it.size >= 2 }
                                        ?.let { RouteLine(route, it) }
                                }
                            }.awaitAll()
                    }.filterNotNull()
            if (lines.isEmpty()) return@coroutineScope null
            val stops = lines.flatMap { line -> line.stops.map { Place(it.name, it.lat, it.lng) } }
            Snapshot(lines, PlaceDirectory(stops))
        }

    private companion object {
        const val BATCH = 20
    }
}
