package com.samuelokello.mwenyeji.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.ui.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun SignInPromptSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Box(modifier = modifier.fillMaxSize()) {
        // Scrim
        AnimatedVisibility(visible = visible) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismiss,
                        ),
            )
        }

        // Sheet
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(colors.surface)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Drag handle
                Box(
                    modifier =
                        Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.outlineVariant),
                )

                Spacer(Modifier.height(24.dp))

                // Title
                Text(
                    text = stringResource(R.string.sign_in_to_share_your_knowledge),
                    style = typography.headlineSmall,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.contributing_requires_a_google_account_so_the_community_knows_who_to_trust),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp))

                // Value props
                SignInBenefit(
                    icon = Icons.Outlined.Route,
                    title = stringResource(R.string.your_routes_your_identity),
                    body = stringResource(R.string.contributions_are_linked_to_your_account_so_you_get_credit_for_your_knowledge),
                )
                Spacer(Modifier.height(16.dp))
                SignInBenefit(
                    icon = Icons.Outlined.Verified,
                    title = stringResource(R.string.trusted_community),
                    body = stringResource(R.string.signed_in_contributors_build_reputation_over_time_the_community_knows_who_to_trust),
                )
                Spacer(Modifier.height(16.dp))
                SignInBenefit(
                    icon = Icons.Outlined.Groups,
                    title = stringResource(R.string.browsing_stays_free),
                    body = stringResource(R.string.no_account_needed_to_read_routes_sign_in_only_when_you_want_to_contribute),
                )

                Spacer(Modifier.height(32.dp))

                // Google Sign-In button
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        modifier = Modifier.size(40.dp),
                    )
                } else {
                    MwenyejiButton(
                        text = stringResource(R.string.continue_with_google),
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Dismiss link
                Text(
                    text = stringResource(R.string.maybe_later),
                    style = typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    modifier =
                        Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp),
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SignInBenefit(icon: ImageVector, title: String, body: String) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(22.dp),
        )
        Column {
            Text(
                text = title,
                style = typography.labelMedium,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = body,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
