package com.samuelokello.mwenyeji.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.presentation.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

/**
 * Usage pattern:
 *   Build a thin wrapper for each specific empty state in the app
 *   (NoStagesNearby, NoGuidesYet, etc.) rather than passing raw strings
 *   at each call site. This keeps the call sites clean and the copy
 *   easy to find and update.
 *
 * @param icon          Vector icon shown inside the circular container.
 * @param iconTint      Icon foreground color. Defaults to warning (amber) for
 *                      location-related states, primary (green) for actionable states.
 * @param iconBackground Background of the circular container.
 * @param heading       Short heading. Keep under 4 words.
 * @param body          One or two sentences. Explains why and what to do.
 * @param hintText      Smaller secondary text below the button. Null hides it.
 * @param modifier      Applied to the root Column.
 * @param ctaContent    a call to action content
 */
@Composable
fun MwenyejiEmptyState(
    icon: ImageVector,
    heading: String,
    body: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MwenyejiTheme.colorScheme.warning,
    iconBackground: Color = MwenyejiTheme.colorScheme.warning.copy(alpha = 0.15f),
    iconSize: Dp = 32.dp,
    containerSize: Dp = 72.dp,
    hintText: String? = null,
    ctaContent: @Composable () -> Unit = {},
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(containerSize)
                    .background(color = iconBackground, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = heading,
            style = typography.headlineSmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = body,
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // CTA
        ctaContent()

        // Hint text
        if (hintText != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = hintText,
                style = typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
