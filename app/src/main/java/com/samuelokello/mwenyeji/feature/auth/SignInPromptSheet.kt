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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun SignInPromptSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scrim(
            visible = visible,
            onDismiss = onDismiss,
            dismissEnabled = !isLoading,
        )

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            SheetContent(
                isLoading = isLoading,
                onGoogleSignIn = onGoogleSignIn,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun Scrim(visible: Boolean, onDismiss: () -> Unit, dismissEnabled: Boolean) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                    .clickable(
                        enabled = dismissEnabled,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
        )
    }
}

@Composable
private fun SheetContent(isLoading: Boolean, onGoogleSignIn: () -> Unit, onDismiss: () -> Unit) {
    val colors =
        MwenyejiTheme.colorScheme
    val typography =
        MwenyejiTheme.typography
    val spacing =
        MwenyejiTheme.spacing
    val cornerRadius =
        MwenyejiTheme.cornerRadius

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = cornerRadius.large,
                        topEnd = cornerRadius.large,
                    ),
                ).background(colors.surface)
                .padding(
                    horizontal = spacing.large,
                    vertical = spacing.extraLarge,
                ).windowInsetsPadding(WindowInsets.navigationBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DragHandle()
        Spacer(Modifier.height(spacing.large))

        Text(
            text = stringResource(R.string.sign_in_to_share_your_knowledge),
            style = typography.headlineSmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.small))
        Text(
            text =
                stringResource(
                    R.string.contributing_requires_a_google_account_so_the_community_knows_who_to_trust,
                ),
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.extraLarge))

        BenefitsList(benefits = rememberBenefits())

        Spacer(Modifier.height(spacing.extraLarge))

        SignInButton(
            isLoading = isLoading,
            onClick = onGoogleSignIn,
        )

        Spacer(Modifier.height(spacing.large))

        Text(
            text = stringResource(R.string.maybe_later),
            style = typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier =
                Modifier
                    .clickable(enabled = !isLoading, onClick = onDismiss)
                    .padding(spacing.small),
        )
        Spacer(Modifier.height(spacing.small))
    }
}

@Composable
private fun SignInButton(isLoading: Boolean, onClick: () -> Unit) {
    val colors =
        MwenyejiTheme.colorScheme
    val sizes =
        MwenyejiTheme.sizes

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        MwenyejiButton(
            text = stringResource(R.string.continue_with_google),
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        if (isLoading) {
            CircularProgressIndicator(
                color = colors.onPrimary,
                strokeWidth = sizes.borderWidthThick,
                modifier = Modifier.size(sizes.iconExtraSmall),
            )
        }
    }
}

@Composable
private fun DragHandle() {
    val sizes =
        MwenyejiTheme.sizes
    val cornerRadius =
        MwenyejiTheme.cornerRadius
    Box(
        modifier =
            Modifier
                .width(DRAG_HANDLE_WIDTH)
                .height(sizes.borderWidthThick * 2) // 4.dp
                .clip(RoundedCornerShape(cornerRadius.small))
                .background(MwenyejiTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun BenefitsList(benefits: List<Benefit>) {
    val spacing = MwenyejiTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        benefits.forEach { benefit ->
            SignInBenefit(
                icon = benefit.icon,
                title = stringResource(benefit.titleRes),
                body = stringResource(benefit.bodyRes),
            )
        }
    }
}

@Composable
private fun SignInBenefit(icon: ImageVector, title: String, body: String) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography
    val spacing = MwenyejiTheme.spacing
    val sizes = MwenyejiTheme.sizes

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(sizes.iconSmall),
        )
        Column {
            Text(title, style = typography.labelMedium, color = colors.onSurface)
            Spacer(Modifier.height(spacing.extraSmall / 2))
            Text(body, style = typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

@Immutable
private data class Benefit(
    val icon: ImageVector,
    val titleRes: Int,
    val bodyRes: Int,
)

@Composable
private fun rememberBenefits(): List<Benefit> =
    remember {
        listOf(
            Benefit(
                icon = Icons.Outlined.Route,
                titleRes = R.string.your_routes_your_identity,
                bodyRes = R.string.contributions_are_linked_to_your_account_so_you_get_credit_for_your_knowledge,
            ),
            Benefit(
                icon = Icons.Outlined.Verified,
                titleRes = R.string.trusted_community,
                bodyRes = R.string.signed_in_contributors_build_reputation_over_time_the_community_knows_who_to_trust,
            ),
            Benefit(
                icon = Icons.Outlined.Groups,
                titleRes = R.string.browsing_stays_free,
                bodyRes = R.string.no_account_needed_to_read_routes_sign_in_only_when_you_want_to_contribute,
            ),
        )
    }

private const val SCRIM_ALPHA = 0.5f
private val DRAG_HANDLE_WIDTH = 40.dp

@Preview(name = "Idle", showBackground = true)
@Composable
private fun SignInPromptSheetPreview_Idle() {
    MwenyejiAppTheme {
        SignInPromptSheet(
            visible = true,
            onDismiss = {},
            onGoogleSignIn = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun SignInPromptSheetPreview_Loading() {
    MwenyejiAppTheme {
        SignInPromptSheet(
            visible = true,
            onDismiss = {},
            onGoogleSignIn = {},
            isLoading = true,
        )
    }
}
