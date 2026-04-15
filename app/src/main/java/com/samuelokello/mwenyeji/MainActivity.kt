package com.samuelokello.mwenyeji

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.samuelokello.mwenyeji.core.InAppUpdateManager
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private lateinit var inAppUpdateManager: InAppUpdateManager

    private val snackbarManager: SnackbarManager by inject<SnackbarManager>()

    private val updateLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode != RESULT_OK) {
                Log.w("InAppUpdate", "Update flow failed or cancelled: ${result.resultCode}")
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()
        window.isNavigationBarContrastEnforced = false

        inAppUpdateManager =
            InAppUpdateManager(this).apply {
                onUpdateDownloaded = { showUpdateReadySnackbar() }
                registerListener()
                checkForUpdate(updateLauncher)
            }

        setContent {
            MwenyejiAppTheme {
                App(snackbarManager = snackbarManager)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager.checkForResumeUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateManager.unregisterListener()
    }

    private fun showUpdateReadySnackbar() {
        snackbarManager.showInfo(
            message = "Update ready — restart to apply",
            actionLabel = "Restart",
            onAction = { inAppUpdateManager.completeUpdate() },
        )
    }
}
