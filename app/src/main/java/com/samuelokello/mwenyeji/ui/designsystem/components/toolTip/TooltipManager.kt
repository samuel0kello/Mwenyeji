package com.samuelokello.mwenyeji.ui.designsystem.components.toolTip

import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TooltipManager(
    private val prefs: MwenyejiPrefs,
) {
    fun shouldShow(key: TooltipKey): Flow<Boolean> = prefs.getTooltipShown(key.name).map { shown -> !shown }

    suspend fun markShown(key: TooltipKey) {
        prefs.setTooltipShown(key.name)
    }
}
