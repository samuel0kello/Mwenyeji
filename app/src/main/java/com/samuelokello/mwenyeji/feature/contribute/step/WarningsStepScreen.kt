package com.samuelokello.mwenyeji.feature.contribute.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.feature.contribute.ContributeActions
import com.samuelokello.mwenyeji.feature.contribute.ContributeState
import com.samuelokello.mwenyeji.feature.feed.TimeOfDayChip
import com.samuelokello.mwenyeji.presentation.designsystem.components.inputFields.MwenyejiInputField
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WarningsStepScreen(state: ContributeState, onAction: (ContributeActions) -> Unit, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "WARNINGS",
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            MwenyejiInputField(
                value = state.warnings,
                onValueChange = { onAction(ContributeActions.WarningsChanged(it)) },
                placeholder = {
                    Text(
                        text = "e.g., Don't board from Tom Mboya during rush hour...",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                // multiline feel ,
                singleLine = false,
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TAGS (SELECT ALL THAT APPLY)",
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RouteTag.entries.forEach { tag ->
                    TimeOfDayChip(
                        title = tag.displayName,
                        selected = tag in state.selectedTags,
                        onSelected = { onAction(ContributeActions.TagToggled(tag)) },
                    )
                }
            }
        }
    }
}
