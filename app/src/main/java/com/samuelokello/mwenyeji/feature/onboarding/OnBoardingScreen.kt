package com.samuelokello.mwenyeji.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun OnBoardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is OnboardingNavigationEvent.NavigateToHome -> onFinish()
            }
        }
    }

    OnBoardingContent(
        pages = viewModel.pages,
        currentPage = currentPage,
        onNextPage = viewModel::onNextPage,
        onFinish = viewModel::onFinish
    )
}


@Composable
fun OnBoardingContent(
    pages: List<Page>,
    currentPage: Int,
    onNextPage: () -> Unit,
    onFinish: () -> Unit
) {
    Scaffold(
        containerColor = MwenyejiTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            key(currentPage) {
                val page = pages[currentPage]
                OnboardingPage(
                    title = stringResource(page.title),
                    description = stringResource(page.description),
                    imageRes = page.drawable,
                    buttonText = page.btnText,
                    currentPage = currentPage,
                    totalPages = pages.size,
                    onButtonClick = {
                        if (currentPage < pages.lastIndex) onNextPage()
                        else onFinish()
                    }
                )
            }
        }
    }
}
