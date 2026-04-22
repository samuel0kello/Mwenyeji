package com.samuelokello.mwenyeji.feature.onboarding.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.feature.onboarding.componenets.GridBackground
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ScreenBottom

@Composable
fun OnboardingPage(isActive: Boolean, label: String, title: String, subtitle: String, content: @Composable (Boolean) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        GridBackground(modifier = Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                content(isActive)
            }

            ScreenBottom(
                label = label,
                title = title,
                subtitle = subtitle,
            )

            // Space for the static "Continue" button and progress indicators
            // Adjust this height to match the height of your static footer in OnboardingScreen
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
