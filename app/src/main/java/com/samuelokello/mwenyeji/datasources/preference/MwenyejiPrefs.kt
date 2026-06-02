package com.samuelokello.mwenyeji.datasources.preference

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "mwenyeji_prefs")

interface MwenyejiPrefs {
    suspend fun setOnBoardingComplete(isComplete: Boolean)

    fun isOnBoardingComplete(): Flow<Boolean>

    suspend fun setTooltipShown(key: String)

    fun getTooltipShown(key: String): Flow<Boolean>

    suspend fun saveUserType(userType: String)

    suspend fun saveDefaultTimeOfDay(timeOfDay: String)

    fun getUserType(): Flow<String?>

    fun getDefaultTimeOfDay(): Flow<String?>
}

class MwenyejiPrefsImpl(
    private val context: Context,
) : MwenyejiPrefs {
    override suspend fun setOnBoardingComplete(isComplete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = isComplete }
    }

    override fun isOnBoardingComplete(): Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }

    override suspend fun setTooltipShown(key: String) {
        context.dataStore.edit { it[tooltipKey(key)] = true }
    }

    override fun getTooltipShown(key: String): Flow<Boolean> = context.dataStore.data.map { it[tooltipKey(key)] ?: false }

    override suspend fun saveUserType(userType: String) {
        context.dataStore.edit { it[KEY_USER_TYPE] = userType }
    }

    override fun getUserType(): Flow<String?> = context.dataStore.data.map { it[KEY_USER_TYPE] }

    override suspend fun saveDefaultTimeOfDay(timeOfDay: String) {
        context.dataStore.edit { it[KEY_DEFAULT_TIME_OF_DAY] = timeOfDay }
    }

    override fun getDefaultTimeOfDay(): Flow<String?> = context.dataStore.data.map { it[KEY_DEFAULT_TIME_OF_DAY] }

    companion object {
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

        private fun tooltipKey(key: String) = booleanPreferencesKey("tooltip_$key")

        private val KEY_USER_TYPE = stringPreferencesKey("user_type")
        private val KEY_DEFAULT_TIME_OF_DAY = stringPreferencesKey("time_of_day")
    }
}
