package com.samuelokello.mwenyeji.core.di

import com.samuelokello.mwenyeji.core.InAppUpdateManager
import com.samuelokello.mwenyeji.ui.designsystem.components.toolTip.TooltipManager
import org.koin.dsl.module

val coreModule =
    module {
        single { InAppUpdateManager(get()) }
        single { TooltipManager(get()) }
    }
