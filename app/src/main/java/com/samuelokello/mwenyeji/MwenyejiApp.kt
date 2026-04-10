package com.samuelokello.mwenyeji

import android.app.Application
import android.util.Log
import com.samuelokello.mwenyeji.datasources.firebase.FirebaseService
import com.samuelokello.mwenyeji.di.mwenyejiModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MwenyejiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MwenyejiApp)
            modules(mwenyejiModules)
        }

        val firebaseService = GlobalContext.get().get<FirebaseService>()

        CoroutineScope(Dispatchers.IO).launch {
            val uid = firebaseService.signInAnonymously()
            Log.d("MwenyejiApp", "Anonymous UID: $uid")
        }
    }
}
