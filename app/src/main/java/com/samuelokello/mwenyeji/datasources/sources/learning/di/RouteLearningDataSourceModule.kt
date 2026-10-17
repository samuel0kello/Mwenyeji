package com.samuelokello.mwenyeji.datasources.sources.learning.di

import com.samuelokello.mwenyeji.datasources.sources.learning.FirebaseRouteLearningRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.learning.RouteLearningRemoteDataSource
import org.koin.dsl.module

val routeLearningDataSourceModule =
    module {
        single<RouteLearningRemoteDataSource> { FirebaseRouteLearningRemoteDataSource(firestore = get()) }
    }
