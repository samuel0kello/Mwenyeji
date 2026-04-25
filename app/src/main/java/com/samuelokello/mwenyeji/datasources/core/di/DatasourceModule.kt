package com.samuelokello.mwenyeji.datasources.core.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.samuelokello.mwenyeji.datasources.core.network.helpers.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val coreDataSourceModule =
    module {
        single<CoroutineContext> { Dispatchers.IO }

        single<FirebaseAuth> { Firebase.auth }
        single<FirebaseFirestore> { Firebase.firestore }

        single<HttpClient> { createHttpClient(engine = Android) }
    }
