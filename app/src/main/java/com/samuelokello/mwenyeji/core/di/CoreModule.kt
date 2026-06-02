package com.samuelokello.mwenyeji.core.di

import com.samuelokello.mwenyeji.core.InAppUpdateManager
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.TooltipManager
import org.koin.dsl.module

val coreModule =
    module {
        single { InAppUpdateManager(get()) }
        single { TooltipManager(get()) }
    }
