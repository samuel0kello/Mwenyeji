package com.samuelokello.mwenyeji.data.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.core.content.edit

class DeviceIdProvider(private val context: Context) {

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        )

        return if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c") {
            // Fallback — generate a UUID and store it in SharedPreferences
            getOrCreateFallbackId()
        } else {
            androidId
        }
    }

    private fun getOrCreateFallbackId(): String {
        val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        val existing = prefs.getString("device_id", null)
        if (existing != null) return existing

        val newId = java.util.UUID.randomUUID().toString()
        prefs.edit { putString("device_id", newId) }
        return newId
    }
}