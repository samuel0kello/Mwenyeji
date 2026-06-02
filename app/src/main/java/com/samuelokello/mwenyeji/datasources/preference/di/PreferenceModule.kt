package com.samuelokello.mwenyeji.datasources.preference.di

import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefsImpl
import org.koin.dsl.module

val preferenceModule =
    module {
        single<MwenyejiPrefs> { MwenyejiPrefsImpl(get()) }
    }
