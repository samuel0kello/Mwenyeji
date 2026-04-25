package com.samuelokello.mwenyeji.feature.auth.di

import com.mapbox.maps.extension.style.model.model
import com.samuelokello.mwenyeji.feature.auth.SessionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAuthModule =
    module {
        viewModelOf(::SessionViewModel)
    }
