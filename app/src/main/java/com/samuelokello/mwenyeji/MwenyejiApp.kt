package com.samuelokello.mwenyeji

import android.app.Application
import com.samuelokello.mwenyeji.di.mwenyejiModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MwenyejiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MwenyejiApp)
            modules(mwenyejiModules)
        }
    }
}
