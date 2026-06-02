package com.samuelokello.mwenyeji.datasources.core.firebase

import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import kotlinx.coroutines.CancellationException

/**
 * Wraps a Firebase suspending call into a [NetworkResult].
 * Always rethrows [CancellationException] so structured concurrency is preserved.
 */
suspend inline fun <T> safeFirebaseCall(crossinline block: suspend () -> T): NetworkResult<T> =
    try {
        NetworkResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        NetworkResult.Error(FirebaseErrorMapper.map(e))
    }
