package com.samuelokello.mwenyeji.feature.feed.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiHeader

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    FeedScreenContent()
}


@Composable
internal fun FeedScreenContent(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            MwenyejiHeader(
                title = "Where to?",
                subTitle = "Find local ways to move around Nairobi",
                canNavigateBack = false,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {  }
        }
    }
}

@Preview
@Composable
fun FeedScreenContentPrev(modifier: Modifier = Modifier) {
    FeedScreenContent()
}