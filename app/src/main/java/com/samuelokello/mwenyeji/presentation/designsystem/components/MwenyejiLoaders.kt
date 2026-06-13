package com.samuelokello.mwenyeji.presentation.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MwenyejiLoadingIndicator() {
    LoadingIndicator(
        color = MwenyejiTheme.colorScheme.primary,
        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons.take(2),
    )
}

@Preview
@Composable
private fun MwenyejiLoadingIndicatorPreview() {
    MwenyejiAppTheme { }
    MwenyejiLoadingIndicator()
}
