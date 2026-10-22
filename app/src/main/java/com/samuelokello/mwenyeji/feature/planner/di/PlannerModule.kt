package com.samuelokello.mwenyeji.feature.planner.di

import com.samuelokello.mwenyeji.core.location.CurrentLocation
import com.samuelokello.mwenyeji.core.location.FusedCurrentLocation
import com.samuelokello.mwenyeji.feature.planner.data.RouteNetwork
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning
import com.samuelokello.mwenyeji.feature.planner.ui.AskViewModel
import com.samuelokello.mwenyeji.feature.planner.ui.PlanTripViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val plannerModule =
    module {
        single<CurrentLocation> { FusedCurrentLocation(androidContext()) }
        // One copy of the route network for the whole app run.
        single { RouteNetwork(routes = get()) }
        factory { TripPlanning(network = get(), routes = get(), search = get(), location = get()) }
        viewModelOf(::PlanTripViewModel)
        viewModelOf(::AskViewModel)
    }
