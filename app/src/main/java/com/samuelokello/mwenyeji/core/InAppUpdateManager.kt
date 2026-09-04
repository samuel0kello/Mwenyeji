package com.samuelokello.mwenyeji.core

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManager(
    context: Context,
) {
    private val tag = "InAppUpdateManager"
    private val appUpdateManager: AppUpdateManager =
        AppUpdateManagerFactory.create(context.applicationContext ?: context)

    private val listener =
        InstallStateUpdatedListener { state ->
            if (state == null) return@InstallStateUpdatedListener
            Log.d(tag, "InstallStatus: ${state.installStatus()}")
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Log.i(tag, "Update downloaded, notifying listener")
                onUpdateDownloaded?.invoke()
            }
        }

    var onUpdateDownloaded: (() -> Unit)? = null

    fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (info == null) return@addOnSuccessListener
                    val availability = info.updateAvailability()
                    Log.d(tag, "Update availability: $availability")

                    if (availability == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {
                        Log.i(tag, "Update available, starting flexible flow")
                        appUpdateManager.startUpdateFlowForResult(
                            info,
                            launcher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        )
                    }
                }.addOnFailureListener { e ->
                    Log.e(tag, "Failed to check for updates", e)
                }
        } catch (e: Exception) {
            Log.e(tag, "Exception during checkForUpdate", e)
        }
    }

    fun checkForResumeUpdate() {
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (info == null) return@addOnSuccessListener
                    if (info.installStatus() == InstallStatus.DOWNLOADED) {
                        Log.i(tag, "Resume check: update already downloaded")
                        onUpdateDownloaded?.invoke()
                    }
                }.addOnFailureListener { e ->
                    Log.e(tag, "Failed to check for resume update", e)
                }
        } catch (e: Exception) {
            Log.e(tag, "Exception during checkForResumeUpdate", e)
        }
    }

    fun completeUpdate() {
        try {
            Log.i(tag, "Completing update (restart requested)")
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            Log.e(tag, "Exception during completeUpdate", e)
        }
    }

    fun registerListener() {
        try {
            Log.d(tag, "Registering listener")
            appUpdateManager.registerListener(listener)
        } catch (e: Exception) {
            Log.e(tag, "Exception during registerListener", e)
        }
    }

    fun unregisterListener() {
        try {
            Log.d(tag, "Unregistering listener")
            appUpdateManager.unregisterListener(listener)
        } catch (e: Exception) {
            Log.e(tag, "Exception during unregisterListener", e)
        }
    }
}
