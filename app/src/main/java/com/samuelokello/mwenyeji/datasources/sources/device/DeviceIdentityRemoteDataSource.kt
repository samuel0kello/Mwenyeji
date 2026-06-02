package com.samuelokello.mwenyeji.datasources.sources.device

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.auth.AuthRemoteDataSource
import kotlinx.coroutines.tasks.await

interface DeviceIdentityRemoteDataSource {
    /**
     * Returns a stable UID for [deviceId], creating one (via anonymous sign-in)
     * and persisting the mapping on first call.
     */
    suspend fun getOrCreateStableUserId(deviceId: String): NetworkResult<String>
}

internal class FirebaseDeviceIdentityRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: AuthRemoteDataSource,
) : DeviceIdentityRemoteDataSource {
    private val collection get() = firestore.collection(COLLECTION)

    override suspend fun getOrCreateStableUserId(deviceId: String): NetworkResult<String> =
        safeFirebaseCall {
            val existing =
                collection
                    .document(deviceId)
                    .get()
                    .await()
                    .getString(FIELD_UID)
            if (existing != null) return@safeFirebaseCall existing

            val uid =
                when (val result = auth.signInAnonymously()) {
                    is NetworkResult.Success -> result.data
                    is NetworkResult.Error -> error("Anonymous sign-in failed: ${result.error.technicalMessage}")
                }

            collection
                .document(deviceId)
                .set(
                    mapOf(
                        FIELD_UID to uid,
                        FIELD_DEVICE_ID to deviceId,
                        FIELD_CREATED_AT to Timestamp.now(),
                    ),
                ).await()

            uid
        }

    private companion object {
        const val COLLECTION = "device_identities"
        const val FIELD_UID = "uid"
        const val FIELD_DEVICE_ID = "deviceId"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
