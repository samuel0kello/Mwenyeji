package com.samuelokello.mwenyeji.datasources.sources.routes

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samuelokello.mwenyeji.datasources.core.firebase.FirebaseErrorMapper
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.RouteDto
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.RouteStopsDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface RoutesRemoteDataSource {
    /**
     * Real-time stream of all routes ordered by confirmedCount descending.
     * Time-of-day filtering and proximity sorting are done client-side in FeedViewModel
     * because the total dataset (148 routes) is small enough that fetching all
     * and filtering in memory is faster and cheaper than multiple geo queries.
     *
     * The [timeOfDay] parameter is kept for interface compatibility but is no
     * longer used server-side — pass it through to the ViewModel for client filtering.
     */
    fun observeRoutes(): Flow<NetworkResult<List<RouteDto>>>

    fun observeRouteById(id: String): Flow<NetworkResult<RouteDto?>>

    suspend fun submitRoute(dto: RouteDto): NetworkResult<String>

    /**
     * Fetches the ordered stop list for a route — only called when the
     * route detail screen opens, not during feed loading.
     */
    suspend fun getRouteStops(routeId: String): NetworkResult<RouteStopsDto?>
}

internal class FirebaseRoutesRemoteDataSource(
    private val firestore: FirebaseFirestore,
) : RoutesRemoteDataSource {
    private val routesCollection
        get() = firestore.collection(RoutesSchema.COLLECTION)

    private val routeStopsCollection
        get() = firestore.collection(RoutesSchema.ROUTE_STOPS_COLLECTION)

    override fun observeRoutes(): Flow<NetworkResult<List<RouteDto>>> =
        callbackFlow {
            val listener =
                routesCollection
                    .orderBy(RoutesSchema.Fields.CONFIRMED_COUNT, Query.Direction.DESCENDING)
                    .limit(ROUTES_LIMIT)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(NetworkResult.Error(FirebaseErrorMapper.map(error)))
                            return@addSnapshotListener
                        }
                        val dtos =
                            snapshot
                                ?.documents
                                ?.mapNotNull { it.toObject(RouteDto::class.java)?.copy(id = it.id) }
                                .orEmpty()
                        trySend(NetworkResult.Success(dtos))
                    }
            awaitClose { listener.remove() }
        }

    override fun observeRouteById(id: String): Flow<NetworkResult<RouteDto?>> =
        callbackFlow {
            val listener =
                routesCollection
                    .document(id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(NetworkResult.Error(FirebaseErrorMapper.map(error)))
                            return@addSnapshotListener
                        }
                        val dto = snapshot?.toObject(RouteDto::class.java)?.copy(id = snapshot.id)
                        trySend(NetworkResult.Success(dto))
                    }
            awaitClose { listener.remove() }
        }

    override suspend fun submitRoute(dto: RouteDto): NetworkResult<String> =
        safeFirebaseCall {
            val ref = routesCollection.document()
            ref.set(dto.copy(id = ref.id)).await()
            ref.id
        }

    override suspend fun getRouteStops(routeId: String): NetworkResult<RouteStopsDto?> =
        safeFirebaseCall {
            val snapshot = routeStopsCollection.document(routeId).get().await()
            snapshot.toObject(RouteStopsDto::class.java)
        }

    private companion object {
        // 200 covers all current routes (148) with headroom for growth.
        // Revisit when approaching 500 — at that scale geo queries become worthwhile.
        const val ROUTES_LIMIT = 200L
    }
}
