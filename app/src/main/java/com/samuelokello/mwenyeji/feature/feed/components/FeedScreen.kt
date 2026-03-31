package com.samuelokello.mwenyeji.feature.feed.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiHeader
import com.samuelokello.mwenyeji.ui.designsystem.components.card.MwenyejiCard

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
            item {

            }
        }
    }
}

@Composable
fun RouteSearchCard(modifier: Modifier = Modifier) {
    MwenyejiCard(

    ) {
        Column() {

        }
    }
}


@Composable
fun LocationField(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    placeHolder: String,
    trailingChevron: ImageVector
) {
//    TextField(
//        state =
//    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {

}

@Composable
fun TimeOfDaySelector(
    modifier: Modifier = Modifier,
    selected: TimeOfDay,
    onSelected: (TimeOfDay) -> Unit
) {

}

@Composable
fun TimeOfDayChip(modifier: Modifier = Modifier) {

}


enum class TimeOfDay {
    MORNING,
    MIDDAY,
    EVENING
}

@Preview
@Composable
fun FeedScreenContentPrev(modifier: Modifier = Modifier) {
    FeedScreenContent()
}