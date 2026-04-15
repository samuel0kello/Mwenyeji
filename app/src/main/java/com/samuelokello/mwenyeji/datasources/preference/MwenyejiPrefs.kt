package com.samuelokello.mwenyeji.datasources.preference

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "mwenyeji_prefs")

interface MwenyejiPrefs {
    suspend fun setOnBoardingComplete(isComplete: Boolean)

    fun isOnBoardingComplete(): Flow<Boolean>
}

class MwenyejiPrefsImpl(
    private val context: Context,
) : MwenyejiPrefs {
    override suspend fun setOnBoardingComplete(isComplete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = isComplete }
    }

    override fun isOnBoardingComplete(): Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }

    companion object {
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }
}
