package com.samuelokello.mwenyeji.ui.designsystem.components.toolTip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

enum class TooltipKey {
    FAB_CONTRIBUTE,

    //    VERDICT_BUTTONS, // Route detail: "Confirm if this still works"
//    SEARCH_BAR, // Feed: "Search by stage, area or destination"
    TIME_OF_DAY_CHIPS, // Feed: "Filter by time of day"
}

/**
 * A one-time animated tooltip that auto-dismisses after.
 * Tap it to dismiss early.
 */
@Composable
fun MwenyejiTooltip(visible: Boolean, text: String, modifier: Modifier = Modifier, emoji: String = "💡", onDismiss: () -> Unit = {}) {
    val colors = MwenyejiTheme.colorScheme

    if (!visible) return

    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceContainer)
                .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text = text,
            style = MwenyejiTheme.typography.bodySmall,
            color = colors.onSurface,
        )
    }
}
