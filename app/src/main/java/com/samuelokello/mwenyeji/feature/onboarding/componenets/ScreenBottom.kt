package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun ScreenBottom(label: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MwenyejiTheme.typography.labelSmall,
            color = MwenyejiTheme.colorScheme.primaryLight,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MwenyejiTheme.typography.headlineMedium,
            color = MwenyejiTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MwenyejiTheme.typography.bodyMedium,
            color = MwenyejiTheme.colorScheme.secondary,
        )
    }
}
