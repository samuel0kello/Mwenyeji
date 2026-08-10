package com.samuelokello.mwenyeji

import android.app.Application
import android.util.Log
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.auth.AuthRemoteDataSource
import com.samuelokello.mwenyeji.di.mwenyejiModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MwenyejiApp : Application() {
    private lateinit var authRemoteDataSource: AuthRemoteDataSource

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) androidLogger(Level.ERROR)
            androidContext(this@MwenyejiApp)
            modules(mwenyejiModules)
        }

        authRemoteDataSource = get()

        bootstrapAnonymousSession()
    }

    /**
     * Ensure the app has a Firebase UID from the very first launch so that features
     * which write to Firestore (e.g. confirmations) always have a stable identity.
     * No-op if the user is already signed in (checked inside the data source).
     */
    private fun bootstrapAnonymousSession() {
        applicationScope.launch {
            when (val result = authRemoteDataSource.signInAnonymously()) {
                is NetworkResult.Success -> {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Anonymous session ready: ${result.data}")
                }

                is NetworkResult.Error -> {
                    Log.w(TAG, "Anonymous sign-in failed: ${result.error.technicalMessage}")
                }
            }
        }
    }

    private companion object {
        const val TAG = "MwenyejiApp"
    }
}
