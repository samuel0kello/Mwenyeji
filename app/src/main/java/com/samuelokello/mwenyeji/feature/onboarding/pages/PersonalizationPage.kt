package com.samuelokello.mwenyeji.feature.onboarding.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Earbuds
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.feature.onboarding.animation.OnboardingPage
import com.samuelokello.mwenyeji.feature.onboarding.animation.RememberTimelineRunner
import com.samuelokello.mwenyeji.feature.onboarding.animation.timeline
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

enum class UserType(
    val label: String,
    val icon: ImageVector,
    val description: String,
    val defaultTimeOfDay: TimeOfDay,
) {
    DAILY_COMMUTER(
        label = "Daily commuter",
        icon = Icons.Default.Commute,
        description = "I know my routes, just want to stay updated",
        defaultTimeOfDay = TimeOfDay.MORNING_RUSH,
    ),
    EXPLORING(
        label = "Exploring Nairobi",
        icon = Icons.Outlined.DataExploration,
        description = "I need help navigating the city",
        defaultTimeOfDay = TimeOfDay.MIDDAY,
    ),
    CONTRIBUTOR(
        label = "Local expert",
        icon = Icons.Outlined.Earbuds,
        description = "I want to share what I know with others",
        defaultTimeOfDay = TimeOfDay.ANYTIME,
    ),
}

sealed interface PersonalizationEvent {
    data object ShowOptions : PersonalizationEvent
}

@Composable
fun PersonalizationPage(isActive: Boolean, selectedUserType: UserType?, onUserTypeSelected: (UserType) -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    val duration = MwenyejiTheme.duration

    val personalizationTimeline =
        remember(duration) {
            timeline<PersonalizationEvent> {
                step(300) { emit -> emit(PersonalizationEvent.ShowOptions) }
            }
        }
    RememberTimelineRunner(
        isActive = isActive,
        timeline = personalizationTimeline,
        onReset = { showOptions = false },
        onEvent = { event ->
            when (event) {
                PersonalizationEvent.ShowOptions -> showOptions = true
            }
        },
    )

    OnboardingPage(
        isActive = isActive,
        label = "Almost there",
        title = "One quick question",
        subtitle = "We'll personalise your experience based on how you Navigate",
    ) {
        AnimatedVisibility(
            visible = showOptions,
            enter =
                slideInVertically(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    initialOffsetY = { it / 2 },
                ) +
                    fadeIn(
                        tween(
                            durationMillis = duration.NORMAL,
                            easing = MwenyejiTheme.easing.decelerated,
                        ),
                    ),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                UserType.entries.forEach { type ->
                    UserTypeOption(
                        userType = type,
                        isSelected = selectedUserType == type,
                        onClick = { onUserTypeSelected(type) },
                    )
                }
            }
        }
    }
}

@Composable
private fun UserTypeOption(userType: UserType, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MwenyejiTheme.colorScheme
    val borderColor = if (isSelected) colors.primary else colors.border
    val containerColor =
        if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surfaceContainerLow
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor)
                .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(userType.icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userType.label,
                style = MwenyejiTheme.typography.labelMedium,
                color = if (isSelected) colors.primary else colors.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = userType.description,
                style = MwenyejiTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
