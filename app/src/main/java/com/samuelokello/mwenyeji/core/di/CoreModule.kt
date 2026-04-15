package com.samuelokello.mwenyeji.core.di

import com.samuelokello.mwenyeji.core.InAppUpdateManager
import org.koin.dsl.module

val coreModule =
    module {
        single { InAppUpdateManager(get()) }
    }
