package com.samuelokello.mwenyeji.feature.contribute.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.presentation.designsystem.components.map.MapPin
import com.samuelokello.mwenyeji.presentation.designsystem.components.map.MwenyejiPinMap
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun RouteMapPreview(
    fromLat: Double,
    fromLng: Double,
    fromDisplayName: String,
    toLat: Double? = null,
    toLng: Double? = null,
    toDisplayName: String? = null,
    onFromPinDragged: (Double, Double) -> Unit,
    onToPinDragged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    val hasTo = toLat != null && toLng != null

    Column(modifier = modifier.fillMaxWidth()) {
        // From pin always; To pin and the line between them once To is confirmed.
        MwenyejiPinMap(
            pins =
                listOfNotNull(
                    MapPin(FROM_PIN, fromLat, fromLng),
                    if (toLat != null && toLng != null) MapPin(TO_PIN, toLat, toLng) else null,
                ),
            onPinMoved = { id, lat, lng -> if (id == FROM_PIN) onFromPinDragged(lat, lng) else onToPinDragged(lat, lng) },
            zoom = if (hasTo) 13.0 else 15.0,
            connectPins = true,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        )

        Spacer(Modifier.height(6.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "FROM: $fromDisplayName",
                style = typography.labelSmall,
                color = colors.primary,
                modifier = Modifier.weight(1f),
            )
            if (toDisplayName != null) {
                Text(
                    text = "TO: $toDisplayName",
                    style = typography.labelSmall,
                    color = colors.error,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Drag pins to adjust exact locations",
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

private const val FROM_PIN = "from"
private const val TO_PIN = "to"
