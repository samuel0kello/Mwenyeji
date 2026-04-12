package com.samuelokello.mwenyeji.datasources.di

import com.samuelokello.mwenyeji.datasources.firebase.FirebaseService
import com.samuelokello.mwenyeji.datasources.firebase.FirebaseServiceImpl
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefsImpl
import org.koin.dsl.module

val dataSourceModule =
    module {
        single<MwenyejiPrefs> { MwenyejiPrefsImpl(get()) }
        single<FirebaseService> { FirebaseServiceImpl() }
    }
