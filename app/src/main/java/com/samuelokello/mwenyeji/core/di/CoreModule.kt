package com.samuelokello.mwenyeji.core.di

import com.samuelokello.mwenyeji.core.InAppUpdateManager
import com.samuelokello.mwenyeji.core.ml.GuideSuggestionEngine
import com.samuelokello.mwenyeji.core.ml.TfLiteGuideSuggestionEngine
import com.samuelokello.mwenyeji.core.network.ConnectivityObserver
import com.samuelokello.mwenyeji.core.network.NetworkConnectivityObserver
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.TooltipManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule =
    module {
        single { InAppUpdateManager(get()) }
        single { TooltipManager(get()) }
        single<GuideSuggestionEngine> { TfLiteGuideSuggestionEngine(get()) }
        single<ConnectivityObserver> { NetworkConnectivityObserver(androidContext()) }
    }
