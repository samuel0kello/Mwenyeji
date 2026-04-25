package com.samuelokello.mwenyeji.datasources.sources.routes

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.core.firebase.FirebaseErrorMapper
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.RouteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface RoutesRemoteDataSource {
    fun observeRoutes(timeOfDay: TimeOfDay): Flow<NetworkResult<List<RouteDto>>>

    fun observeRouteById(id: String): Flow<NetworkResult<RouteDto?>>

    suspend fun submitRoute(dto: RouteDto): NetworkResult<String>
}

internal class FirebaseRoutesRemoteDataSource(
    private val firestore: FirebaseFirestore,
) : RoutesRemoteDataSource {
    private val collection get() = firestore.collection(RoutesSchema.COLLECTION)

    override fun observeRoutes(timeOfDay: TimeOfDay): Flow<NetworkResult<List<RouteDto>>> =
        callbackFlow {
            val listener =
                buildRoutesQuery(timeOfDay).addSnapshotListener { snapshot, error ->
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
                collection.document(id).addSnapshotListener { snapshot, error ->
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
            val ref = collection.document()
            ref.set(dto.copy(id = ref.id)).await()
            ref.id
        }

    private fun buildRoutesQuery(timeOfDay: TimeOfDay): Query =
        if (timeOfDay == TimeOfDay.ANYTIME) {
            collection
                .orderBy(RoutesSchema.Fields.CONFIRMED_COUNT, Query.Direction.DESCENDING)
                .limit(ROUTES_LIMIT)
        } else {
            collection
                .whereEqualTo(RoutesSchema.Fields.BEST_TIME_OF_DAY, timeOfDay.name)
                .orderBy(RoutesSchema.Fields.CONFIRMED_COUNT, Query.Direction.DESCENDING)
                .limit(ROUTES_LIMIT)
        }

    private companion object {
        const val ROUTES_LIMIT = 20L
    }
}
