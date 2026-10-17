package com.samuelokello.mwenyeji.feature.planner.domain

import com.samuelokello.mwenyeji.core.location.CurrentLocation
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.data.repository.SearchRepository
import com.samuelokello.mwenyeji.feature.planner.data.RouteNetwork
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

/**
 * From what the user typed or said to trips they can start. Shared by the "Where to?" screen and chat.
 *
 * Offline first: places come from the stops on the phone and Nairobi's nicknames; only a place we do
 * not know goes to the online search. Facts (routes, stops, saccos, fares) come only from our data.
 */
class TripPlanning(
    private val network: RouteNetwork,
    private val routes: RoutesRepository,
    private val search: SearchRepository,
    private val location: CurrentLocation,
) {
    sealed interface Outcome {
        /** No destination could be read from the text. */
        data object NotATrip : Outcome

        data class UnknownPlace(
            val name: String,
        ) : Outcome

        /** "From here" needs location, and there is none: no permission, or no fix in time. */
        data object NeedLocation : Outcome

        /** No routes on the phone and none could be loaded. */
        data object NoRoutes : Outcome

        data class Walkable(
            val to: Place,
            val metres: Int,
        ) : Outcome

        data class NoDirectRoute(
            val from: Place,
            val to: Place,
        ) : Outcome

        data class Options(
            val from: Place,
            val to: Place,
            val options: List<TripOption>,
        ) : Outcome
    }

    suspend fun plan(text: String): Outcome {
        val parsed = TripRequestParser.parse(text) ?: return Outcome.NotATrip
        val snapshot = network.load() ?: return Outcome.NoRoutes

        val to = resolve(parsed.to, snapshot) ?: return Outcome.UnknownPlace(parsed.to)
        val from =
            if (parsed.from != null) {
                resolve(parsed.from, snapshot) ?: return Outcome.UnknownPlace(parsed.from)
            } else {
                location.get()?.let { (lat, lng) -> Place(HERE, lat, lng) } ?: return Outcome.NeedLocation
            }

        val apart = TripPlanner.metres(from, to)
        if (apart < TripPlanner.WALKABLE_M) return Outcome.Walkable(to, (apart / 10).roundToInt() * 10)

        val options = TripPlanner.plan(from, to, snapshot.lines)
        if (options.isEmpty()) return Outcome.NoDirectRoute(from, to)
        return Outcome.Options(from, to, withGuides(options))
    }

    /** Our own names first; the online search only for places we do not know. */
    private suspend fun resolve(
        name: String,
        snapshot: RouteNetwork.Snapshot,
    ): Place? =
        snapshot.places.find(name)
            ?: (search.search(SearchRequest(name, limit = 1)) as? DataResult.Success)
                ?.data
                ?.firstNotNullOfOrNull { result -> result.coordinates?.let { Place(result.name, it.latitude, it.longitude) } }

    /** The sacco and fare from each route's most confirmed guide, so "look for Super Metro, around 80". */
    private suspend fun withGuides(options: List<TripOption>): List<TripOption> =
        coroutineScope {
            options
                .map { option ->
                    async {
                        val guide =
                            (routes.observeGuides(option.route.id).first() as? DataResult.Success)
                                ?.data
                                ?.maxByOrNull { it.confirmedCount }
                        option.copy(sacco = guide?.sacco?.takeIf { it.isNotBlank() }, fare = guide?.fareKsh?.roundToInt())
                    }
                }.awaitAll()
        }

    companion object {
        /** The name used for "from my location". */
        const val HERE = "Your location"
    }
}
