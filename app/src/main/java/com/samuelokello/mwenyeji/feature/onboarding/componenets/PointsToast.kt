package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.presentation.designsystem.animation.MwenyejiAnimatedVisibility
import com.samuelokello.mwenyeji.presentation.designsystem.animation.MwenyejiAnimations
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.GreenLight

@Composable
fun PointsToast(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    MwenyejiAnimatedVisibility(
        visible = visible,
        enter = MwenyejiAnimations.slideInEnd,
        exit = MwenyejiAnimations.slideOutEnd,
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GreenLight)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("🎉", fontSize = 16.sp)
            Text(
                "+10 points earned!",
                style = MwenyejiTheme.typography.bodySmall,
                color = Color(0xFF050D05),
            )
        }
    }
}
