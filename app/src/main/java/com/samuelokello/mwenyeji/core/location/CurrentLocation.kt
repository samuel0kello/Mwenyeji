package com.samuelokello.mwenyeji.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Where the phone is now, for "from my location". */
interface CurrentLocation {
    fun hasPermission(): Boolean

    /** Latitude and longitude, or null without permission or without a fix in time. */
    suspend fun get(): Pair<Double, Double>?
}

class FusedCurrentLocation(
    context: Context,
) : CurrentLocation {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    override fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }

    @SuppressLint("MissingPermission")
    override suspend fun get(): Pair<Double, Double>? {
        if (!hasPermission()) return null
        return withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val cancellation = CancellationTokenSource()
                client
                    .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                    .addOnSuccessListener { continuation.resume(it?.let { fix -> fix.latitude to fix.longitude }) }
                    .addOnFailureListener { continuation.resume(null) }
                continuation.invokeOnCancellation { cancellation.cancel() }
            }
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
