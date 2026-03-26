package com.samuelokello.mwenyeji.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun OnboardingPage(
    modifier: Modifier = Modifier,
    @DrawableRes imageRes: Int,
    title: String,
    description: String,
    currentPage: Int,
    totalPages: Int,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 150),
        label = "titleAlpha"
    )

    val descriptionAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 250),
        label = "descriptionAlpha"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    val cardColor = MwenyejiTheme.colorScheme.surfaceContainer
    val circleColor = MwenyejiTheme.colorScheme.primaryDark

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MwenyejiTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .drawBehind {
                    drawRoundRect(
                        color = cardColor,
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .drawBehind {
                        drawCircle(
                            color = circleColor,
                            radius = size.minDimension / 2f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedIcon(
                    icon = imageRes,
                    iconScale = iconScale
                )
            }
        }

        Spacer(modifier = Modifier.height(MwenyejiTheme.spacing.large))

        Text(
            text = title,
            style = MwenyejiTheme.typography.headlineMedium,
            modifier = Modifier.graphicsLayer(alpha = titleAlpha),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MwenyejiTheme.spacing.small))

        Text(
            text = description,
            style = MwenyejiTheme.typography.bodyLarge,
            modifier = Modifier.graphicsLayer(alpha = descriptionAlpha),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        PageIndicator(currentPage = currentPage, totalPages = totalPages)

        Spacer(modifier = Modifier.height(MwenyejiTheme.spacing.extraLarge))

        MwenyejiButton(
            text = buttonText,
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AnimatedIcon(
    @DrawableRes icon: Int,
    iconScale: Float,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier.graphicsLayer(
            scaleX = iconScale,
            scaleY = iconScale,
        )
    )
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    val activeColor = MwenyejiTheme.colorScheme.primary        // active dot — brand green
    val inactiveColor = MwenyejiTheme.colorScheme.outlineVariant // inactive dot — muted, theme-aware

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage

            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicatorWidth"
            )

            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                label = "indicatorColor"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}