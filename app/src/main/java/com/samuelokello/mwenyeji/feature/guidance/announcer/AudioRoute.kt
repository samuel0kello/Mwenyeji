package com.samuelokello.mwenyeji.feature.guidance.announcer

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

/** Where sound would come out right now, as far as guidance cares: privately, or for the whole matatu. */
internal object AudioRoute {
    // Bluetooth audio includes car stereos; in a private car that is fine to speak through.
    private val PRIVATE_OUTPUTS =
        buildSet {
            add(AudioDeviceInfo.TYPE_WIRED_HEADSET)
            add(AudioDeviceInfo.TYPE_WIRED_HEADPHONES)
            add(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
            add(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) add(AudioDeviceInfo.TYPE_USB_HEADSET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(AudioDeviceInfo.TYPE_BLE_HEADSET)
        }

    /** True when earphones, headphones or Bluetooth audio are connected. */
    fun hasPrivateOutput(audioManager: AudioManager): Boolean =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { it.type in PRIVATE_OUTPUTS }
}
