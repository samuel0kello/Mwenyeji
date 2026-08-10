package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    delayPerChar: Long = 60L,
) {
    var displayed by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        displayed = ""
        text.forEach { char ->
            delay(delayPerChar)
            displayed += char
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = stringResource(R.string.cursor))
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                tween(500, easing = LinearEasing),
                RepeatMode.Reverse,
            ),
        label = stringResource(R.string.cursor_blink),
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(displayed, style = style, color = color)
        Box(
            Modifier
                .padding(start = 2.dp)
                .width(2.dp)
                .height(20.dp)
                .alpha(cursorAlpha)
                .background(MwenyejiTheme.colorScheme.primary),
        )
    }
}
