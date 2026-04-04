package com.samuelokello.mwenyeji.feature.feed.di

import com.samuelokello.mwenyeji.feature.feed.FeedViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val feedModule = module {
    viewModelOf(::FeedViewModel)
}