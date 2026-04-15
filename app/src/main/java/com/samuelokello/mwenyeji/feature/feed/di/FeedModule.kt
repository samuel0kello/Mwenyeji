package com.samuelokello.mwenyeji.feature.feed.di

import com.samuelokello.mwenyeji.feature.contribute.ContributeViewModel
import com.samuelokello.mwenyeji.feature.feed.FeedViewModel
import com.samuelokello.mwenyeji.feature.feed.route.AllRoutesViewModel
import com.samuelokello.mwenyeji.feature.feed.route.RouteDetailsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val feedModule =
    module {
        viewModelOf(::FeedViewModel)
        viewModelOf(::AllRoutesViewModel)
        viewModelOf(::RouteDetailsViewModel)
        viewModelOf(::ContributeViewModel)
    }
