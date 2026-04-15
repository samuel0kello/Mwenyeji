package com.samuelokello.mwenyeji.feature.contribute.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.feature.contribute.ContributeActions
import com.samuelokello.mwenyeji.feature.contribute.ContributeState
import com.samuelokello.mwenyeji.feature.feed.TimeOfDayChip
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimingStepScreen(state: ContributeState, onAction: (ContributeActions) -> Unit, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "BEST TIME OF DAY",
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            TimeOfDay.entries.forEach { timeOfDay ->
                TimeOfDayChip(
                    title = timeOfDay.displayName,
                    selected = state.bestTimeOfDay == timeOfDay,
                    onSelected = {
                        val selected =
                            TimeOfDay.entries.first {
                                it.displayName == timeOfDay.displayName
                            }
                        onAction(ContributeActions.TimeOfDaySelected(selected))
                    },
                    modifier = Modifier,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // WHY? field — correctly binds to timingReason, not from
        StepInputField(
            label = "WHY? (OPTIONAL)",
            value = state.timingReason,
            placeholder = "e.g., Highway is clear, fewer matatus...",
            onValueChange = { onAction(ContributeActions.TimingReasonChanged(it)) }, // ← was FromChanged
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimmingPrev() {
    TimingStepScreen(
        state =
            ContributeState(),
        onAction = {},
    )
}
