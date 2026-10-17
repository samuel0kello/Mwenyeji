package com.samuelokello.mwenyeji.datasources.sources.learning

import com.google.firebase.firestore.FirebaseFirestore
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.learning.dto.RideTraceDto
import com.samuelokello.mwenyeji.datasources.sources.learning.dto.RouteVariantDto
import kotlinx.coroutines.tasks.await

interface RouteLearningRemoteDataSource {
    /** Adds one ride's trace to /ride_traces. Firestore queues it offline and sends it later. */
    suspend fun uploadRideTrace(dto: RideTraceDto): NetworkResult<Unit>

    /** The learned variants of one route in one direction. */
    suspend fun getRouteVariants(
        routeId: String,
        direction: String,
    ): NetworkResult<List<RouteVariantDto>>
}

internal class FirebaseRouteLearningRemoteDataSource(
    private val firestore: FirebaseFirestore,
) : RouteLearningRemoteDataSource {
    override suspend fun uploadRideTrace(dto: RideTraceDto): NetworkResult<Unit> =
        safeFirebaseCall {
            firestore.collection(RouteLearningSchema.RIDE_TRACES_COLLECTION).add(dto).await()
            Unit
        }

    override suspend fun getRouteVariants(
        routeId: String,
        direction: String,
    ): NetworkResult<List<RouteVariantDto>> =
        safeFirebaseCall {
            firestore
                .collection(RouteLearningSchema.ROUTE_VARIANTS_COLLECTION)
                .whereEqualTo(RouteLearningSchema.Fields.ROUTE_ID, routeId)
                .whereEqualTo(RouteLearningSchema.Fields.DIRECTION, direction)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(RouteVariantDto::class.java) }
        }
}
