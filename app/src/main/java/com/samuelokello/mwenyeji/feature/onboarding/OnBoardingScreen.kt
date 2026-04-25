package com.samuelokello.mwenyeji.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.feature.onboarding.pages.CommunityPage
import com.samuelokello.mwenyeji.feature.onboarding.pages.FindRoutePage
import com.samuelokello.mwenyeji.feature.onboarding.pages.KnowNairobiPage
import com.samuelokello.mwenyeji.feature.onboarding.pages.PersonalizationPage
import com.samuelokello.mwenyeji.feature.onboarding.pages.ShareKnowledgePage
import com.samuelokello.mwenyeji.ui.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val TOTAL_PAGES = 5

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = koinViewModel(), onFinish: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState { TOTAL_PAGES }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onAction(OnboardingContract.Action.OnPageChanged(pagerState.currentPage))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            if (it is OnboardingContract.Effect.NavigateToHome) onFinish()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .safeContentPadding(),
    ) {
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> {
                    KnowNairobiPage(state.currentPage == 0)
                }

                1 -> {
                    FindRoutePage(state.currentPage == 1)
                }

                2 -> {
                    ShareKnowledgePage(state.currentPage == 2)
                }

                3 -> {
                    CommunityPage(state.currentPage == 3)
                }

                4 -> {
                    PersonalizationPage(
                        isActive = state.currentPage == 4,
                        selectedUserType = state.selectedUserType,
                        onUserTypeSelected = {
                            viewModel.onAction(OnboardingContract.Action.OnUserTypeSelected(it))
                        },
                    )
                }
            }
        }

        // CTA button — disabled on last page until user picks an option
        val isLastPage = state.currentPage == TOTAL_PAGES - 1
        val isButtonEnabled = !isLastPage || state.selectedUserType != null

        MwenyejiButton(
            onClick = {
                scope.launch {
                    viewModel.onAction(OnboardingContract.Action.OnNextClicked)

                    if (state.currentPage < TOTAL_PAGES) {
                        pagerState.animateScrollToPage(state.currentPage + 1)
                    }
                }
            },
            text =
                when {
                    isLastPage -> "Get started →"
                    else -> "Next"
                },
            enabled = isButtonEnabled,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
        )

        if (state.currentPage <= 1) {
            TextButton(
                onClick = {
                    viewModel.onAction(OnboardingContract.Action.OnSkipClicked)
                    onFinish()
                },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Text(text = "Skip", style = MwenyejiTheme.typography.titleMedium)
            }
        }
    }
}
