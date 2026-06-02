package com.samuelokello.mwenyeji.datasources.sources.confirmation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.routes.RoutesSchema
import kotlinx.coroutines.tasks.await

interface ConfirmationsRemoteDataSource {
    suspend fun submitVerdict(routeId: String, userId: String, verdict: Verdict): NetworkResult<Unit>

    suspend fun getUserVerdict(routeId: String, userId: String): NetworkResult<Verdict?>
}

internal class FirebaseConfirmationsRemoteDataSource(
    private val firestore: FirebaseFirestore,
) : ConfirmationsRemoteDataSource {
    private val routes get() = firestore.collection(RoutesSchema.COLLECTION)

    override suspend fun submitVerdict(routeId: String, userId: String, verdict: Verdict): NetworkResult<Unit> =
        safeFirebaseCall {
            val routeRef = routes.document(routeId)
            val confirmationRef =
                routeRef
                    .collection(RoutesSchema.CONFIRMATIONS_SUBCOLLECTION)
                    .document(userId)

            val existing = confirmationRef.get().await()
            val previous =
                existing
                    .takeIf { it.exists() }
                    ?.getString(FIELD_VERDICT)
                    ?.toVerdictOrNull()

            firestore
                .runTransaction { tx ->
                    if (previous == verdict) {
                        tx.delete(confirmationRef)
                        tx.update(routeRef, verdict.countField, FieldValue.increment(-1))
                    } else {
                        tx.set(
                            confirmationRef,
                            mapOf(
                                FIELD_VERDICT to verdict.wireValue,
                                FIELD_USER_ID to userId,
                                FIELD_CONFIRMED_AT to Timestamp.now(),
                            ),
                        )
                        previous?.let {
                            tx.update(
                                routeRef,
                                it.countField,
                                FieldValue.increment(-1),
                            )
                        }
                        tx.update(routeRef, verdict.countField, FieldValue.increment(1))
                        if (verdict == Verdict.CONFIRMED) {
                            tx.update(
                                routeRef,
                                RoutesSchema.Fields.LAST_CONFIRMED_AT,
                                Timestamp.now(),
                            )
                        }
                    }
                }.await()
        }

    override suspend fun getUserVerdict(routeId: String, userId: String): NetworkResult<Verdict?> =
        safeFirebaseCall {
            routes
                .document(routeId)
                .collection(RoutesSchema.CONFIRMATIONS_SUBCOLLECTION)
                .document(userId)
                .get()
                .await()
                .getString(FIELD_VERDICT)
                ?.toVerdictOrNull()
        }

    private val Verdict.wireValue: String get() = name

    private val Verdict.countField: String
        get() =
            when (this) {
                Verdict.CONFIRMED -> RoutesSchema.Fields.CONFIRMED_COUNT
                Verdict.DIDNT_WORK -> RoutesSchema.Fields.DIDNT_WORK_COUNT
                Verdict.OUTDATED -> RoutesSchema.Fields.OUTDATED_COUNT
            }

    private fun String.toVerdictOrNull(): Verdict? = runCatching { Verdict.valueOf(this) }.getOrNull()

    private companion object {
        const val FIELD_VERDICT = "verdict"
        const val FIELD_USER_ID = "userId"
        const val FIELD_CONFIRMED_AT = "confirmedAt"
    }
}
