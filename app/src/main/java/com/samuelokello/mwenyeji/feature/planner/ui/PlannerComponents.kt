package com.samuelokello.mwenyeji.feature.planner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.planner.domain.TripOption
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning.Outcome
import com.samuelokello.mwenyeji.presentation.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.presentation.designsystem.components.button.MwenyejiOutlinedButton
import com.samuelokello.mwenyeji.presentation.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

/** Where the user types or speaks a trip. The mic shows only on phones with a speech recogniser. */
@Composable
fun TripInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    onSubmit: () -> Unit,
    voice: VoiceInput,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(colors.surfaceContainerHigh, shape)
                .border(0.5.dp, colors.border, shape)
                .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
            if (text.isEmpty()) {
                Text(text = placeholder, style = MwenyejiTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                singleLine = true,
                textStyle = MwenyejiTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (voice.available) {
            IconButton(onClick = voice.listen) {
                Icon(
                    painter = painterResource(R.drawable.ic_outline_mic),
                    contentDescription = stringResource(R.string.plan_speak),
                    tint = colors.primary,
                )
            }
        }
        IconButton(onClick = onSubmit, enabled = text.isNotBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_outline_send),
                contentDescription = stringResource(R.string.plan_send),
                tint = if (text.isNotBlank()) colors.primary else colors.onSurfaceVariant,
            )
        }
    }
}

/**
 * What planning came back with: trips to start, or one plain sentence saying why there are none.
 *
 * @param onAllowLocation Shown when planning "from here" needs the location permission.
 */
@Composable
fun PlanOutcomeContent(
    outcome: Outcome,
    onStart: (TripOption) -> Unit,
    onAllowLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (outcome) {
            is Outcome.Options -> {
                Note(stringResource(R.string.plan_trips_to, outcome.to.name, outcome.from.name))
                outcome.options.forEach { option -> TripOptionCard(option = option, onStart = { onStart(option) }) }
            }

            Outcome.NotATrip -> Note(stringResource(R.string.plan_not_a_trip))

            is Outcome.UnknownPlace -> Note(stringResource(R.string.plan_unknown_place, outcome.name))

            Outcome.NeedLocation -> {
                Note(stringResource(R.string.plan_need_location))
                MwenyejiOutlinedButton(text = stringResource(R.string.plan_allow_location), onClick = onAllowLocation)
            }

            Outcome.NoRoutes -> Note(stringResource(R.string.plan_no_routes))

            is Outcome.Walkable -> Note(stringResource(R.string.plan_walkable, outcome.to.name, outcome.metres))

            is Outcome.NoDirectRoute -> Note(stringResource(R.string.plan_no_direct_route, outcome.from.name, outcome.to.name))
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text = text, style = MwenyejiTheme.typography.bodyLarge, color = MwenyejiTheme.colorScheme.onSurface)
}

/** One trip, read top to bottom the way it is travelled: walk, ride, walk, then Start guide. */
@Composable
fun TripOptionCard(
    option: TripOption,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography
    val routeName = option.route.routeNumber ?: option.route.from
    MwenyejiCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colors.border),
        elevation = MwenyejiTheme.elevation.level0,
        containerColor = colors.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text =
                    option.sacco?.let { stringResource(R.string.plan_option_title_sacco, routeName, it) }
                        ?: stringResource(R.string.plan_option_title, routeName),
                style = typography.titleMedium,
                color = colors.onSurface,
            )
            Step(stringResource(R.string.plan_walk_to_stage, option.walkToStageMetres, option.boardingStop.name))
            Step(pluralStringResource(R.plurals.plan_ride, option.stopsToRide, option.stopsToRide, option.alightStop.name))
            if (option.walkFromStopMetres >= MIN_WALK_TO_MENTION_M) {
                Step(stringResource(R.string.plan_walk_from_stop, option.walkFromStopMetres))
            }
            option.fare?.let { Step(stringResource(R.string.plan_fare, it)) }
            MwenyejiButton(
                text = stringResource(R.string.guidance_start_guide),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun Step(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier =
                Modifier
                    .padding(top = 7.dp)
                    .size(6.dp)
                    .background(MwenyejiTheme.colorScheme.primary, RoundedCornerShape(3.dp)),
        )
        Text(text = text, style = MwenyejiTheme.typography.bodyMedium, color = MwenyejiTheme.colorScheme.onSurfaceVariant)
    }
}

// A last few metres to the door are not worth a line.
private const val MIN_WALK_TO_MENTION_M = 100
