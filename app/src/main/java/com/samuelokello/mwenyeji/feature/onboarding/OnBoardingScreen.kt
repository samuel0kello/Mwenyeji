package com.samuelokello.mwenyeji.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.samuelokello.mwenyeji.feature.onboarding.steps.Screen1KnowNairobi
import com.samuelokello.mwenyeji.feature.onboarding.steps.Screen2FindRoute
import com.samuelokello.mwenyeji.feature.onboarding.steps.Screen3ShareKnowledge
import com.samuelokello.mwenyeji.feature.onboarding.steps.Screen4Community
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import kotlinx.coroutines.launch

@Composable
fuOnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    fun next() {
        scope.launch {
            if (pagerState.currentPage < 3) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } else {
                onFinish()
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MwenyejiTheme.colorScheme.surfaceContainer),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> {
                    Screen1KnowNairobi(
                        currentPage = pagerState.currentPage,
                        onNext = ::next,
                        onSkip = onFinish,
                    )
                }

                1 -> {
                    Screen2FindRoute(
                        currentPage = pagerState.currentPage,
                        onNext = ::next,
                        onSkip = onFinish,
                    )
                }

                2 -> {
                    Screen3ShareKnowledge(
                        currentPage = pagerState.currentPage,
                        onNext = ::next,
                        onSkip = onFinish,
                    )
                }

                3 -> {
                    Screen4Community(
                        currentPage = pagerState.currentPage,
                        onFinish = onFinish,
                    )
                }
            }
        }
    }
}
