package com.samuelokello.mwenyeji.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiHeader(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    canNavigateBack: Boolean = false,
    subTitleColor: Color = MwenyejiTheme.colorScheme.primary,
) {
    TopAppBar(
        title = {
            Column(
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MwenyejiTheme.typography.headlineLarge,
                )
                Text(
                    text = subTitle,
                    style = MwenyejiTheme.typography.labelSmall.copy(color = subTitleColor),
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                Icon(
                    Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Navigate Back",
                )
            }
        },
    )
}

@Preview
@Composable
fun MwenyejiHeaderPrev() {
    MwenyejiHeader(
        title = "Where to?",
        subTitle = "Find local ways to move around Nairobi",
        canNavigateBack = true,
        subTitleColor = MwenyejiTheme.colorScheme.primary,
    )
}
