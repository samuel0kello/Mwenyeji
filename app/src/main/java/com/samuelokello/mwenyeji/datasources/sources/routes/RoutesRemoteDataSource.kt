package com.samuelokello.mwenyeji.datasources.sources.routes

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samuelokello.mwenyeji.datasources.core.firebase.FirebaseErrorMapper
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.GuideDto
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.RouteDto
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.RouteStopsDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface RoutesRemoteDataSource {
    /**
     * Real-time stream of all GTFS routes ordered by confirmedCount.
     * ProximityEngine sorts client-side once location is available.
     */
    fun observeRoutes(): Flow<NetworkResult<List<RouteDto>>>

    fun observeRouteById(id: String): Flow<NetworkResult<RouteDto?>>

    /**
     * Ordered stop list for a route.
     * Loaded on demand — not during feed loading.
     */
    suspend fun getRouteStops(routeId: String): NetworkResult<RouteStopsDto?>

    // ── Guides (community knowledge attached to a route) ─────────────────────

    /**
     * Real-time stream of all guides for a route, sorted by confirmedCount.
     * Called when route detail screen opens.
     */
    fun observeGuides(routeId: String): Flow<NetworkResult<List<GuideDto>>>

    /**
     * Submits a new guide to /routes/{routeId}/guides.
     * Returns the new guide document ID.
     */
    suspend fun submitGuide(
        routeId: String,
        dto: GuideDto,
    ): NetworkResult<String>
}

internal class FirebaseRoutesRemoteDataSource(
    private val firestore: FirebaseFirestore,
) : RoutesRemoteDataSource {
    private val routesCollection
        get() = firestore.collection(RoutesSchema.COLLECTION)

    private val routeStopsCollection
        get() = firestore.collection(RoutesSchema.ROUTE_STOPS_COLLECTION)

    private fun guidesCollection(routeId: String) =
        routesCollection
            .document(routeId)
            .collection(RoutesSchema.GUIDES_SUBCOLLECTION)

    override fun observeRoutes(): Flow<NetworkResult<List<RouteDto>>> =
        callbackFlow {
            val listener =
                routesCollection
                    .orderBy(RoutesSchema.Fields.CONFIRMED_COUNT, Query.Direction.DESCENDING)
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

    override suspend fun getRouteStops(routeId: String): NetworkResult<RouteStopsDto?> =
        safeFirebaseCall {
            val snapshot = routeStopsCollection.document(routeId).get().await()
            snapshot.toObject(RouteStopsDto::class.java)
        }

    override fun observeGuides(routeId: String): Flow<NetworkResult<List<GuideDto>>> =
        callbackFlow {
            val listener =
                guidesCollection(routeId)
                    .orderBy(RoutesSchema.Fields.CONFIRMED_COUNT, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(NetworkResult.Error(FirebaseErrorMapper.map(error)))
                            return@addSnapshotListener
                        }
                        val dtos =
                            snapshot
                                ?.documents
                                ?.mapNotNull { it.toObject(GuideDto::class.java)?.copy(id = it.id) }
                                .orEmpty()
                        trySend(NetworkResult.Success(dtos))
                    }
            awaitClose { listener.remove() }
        }

    override suspend fun submitGuide(
        routeId: String,
        dto: GuideDto,
    ): NetworkResult<String> =
        safeFirebaseCall {
            val ref = guidesCollection(routeId).document()
            ref.set(dto.copy(id = ref.id, routeId = routeId)).await()

            // Increment guideCount on the parent route document atomically
            routesCollection
                .document(routeId)
                .update(
                    RoutesSchema.Fields.GUIDE_COUNT,
                    com.google.firebase.firestore.FieldValue
                        .increment(1),
                ).await()

            ref.id
        }
}
