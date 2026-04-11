package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun ScreenBottom(
    label: String,
    title: String,
    subtitle: String,
    page: Int,
    btnText: String,
    onNext: () -> Unit,
    isFinal: Boolean = false,
) {
    Column {
        Text(
            label,
            style = MwenyejiTheme.typography.labelSmall,
            color = MwenyejiTheme.colorScheme.primaryLight
        )
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            style = MwenyejiTheme.typography.headlineMedium,
            color = MwenyejiTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MwenyejiTheme.typography.bodyLarge,
            color = MwenyejiTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(20.dp))
        ProgressDots(total = 4, current = page)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isFinal) 60.dp else 56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MwenyejiTheme.colorScheme.primary,
                contentColor = MwenyejiTheme.colorScheme.onPrimary
            ),
        ) {
            Text(
                "$btnText →",
                style = MwenyejiTheme.typography.bodyLarge,
            )
        }
    }
}