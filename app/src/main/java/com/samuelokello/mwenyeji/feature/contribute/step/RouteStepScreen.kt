package com.samuelokello.mwenyeji.feature.contribute.step

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.feature.contribute.ContributeActions
import com.samuelokello.mwenyeji.feature.contribute.ContributeState
import com.samuelokello.mwenyeji.feature.contribute.components.RouteMapPreview
import com.samuelokello.mwenyeji.presentation.designsystem.components.inputFields.MwenyejiInputField
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

// @OptIn(MapboxExperimental::class)
// @Composable
// fun RouteStepScreen(state: ContributeState, onAction: (ContributeActions) -> Unit, modifier: Modifier = Modifier) {
//    val colors = MwenyejiTheme.colorScheme
//    val typography = MwenyejiTheme.typography
//    Column(modifier = modifier.fillMaxWidth()) {
//        StepInputField(
//            label = "FROM",
//            value = state.from,
//            placeholder = "e.g., CBD, Kencom",
//            onValueChange = { onAction(ContributeActions.FromChanged(it)) },
//            errorMessage = state.errors["from"],
//        )
//
//        // Suggestions dropdown for From
//        AnimatedVisibility(
//            visible = state.fromSuggestions.isNotEmpty(),
//            enter = fadeIn() + expandVertically(),
//        ) {
//            SuggestionsDropdown(
//                suggestions = state.fromSuggestions,
//                onSuggestionClick = { result ->
//                    onAction(ContributeActions.FromSuggestionSelected(result))
//                },
//            )
//        }
//
//        // Map preview once From point is confirmed
//        // Single map preview — shows once From is confirmed, adds To pin when confirmed
//        AnimatedVisibility(
//            visible = state.fromGeoPoint != null && state.fromSuggestions.isEmpty(),
//            enter = fadeIn() + expandVertically(),
//        ) {
//            state.fromGeoPoint?.let { from ->
//                Spacer(Modifier.height(8.dp))
//                RouteMapPreview(
//                    fromLat = from.lat,
//                    fromLng = from.lng,
//                    fromDisplayName = from.displayName,
//                    toLat = state.toGeoPoint?.lat,
//                    toLng = state.toGeoPoint?.lng,
//                    toDisplayName = state.toGeoPoint?.displayName,
//                    onFromPinDragged = { lat, lng ->
//                        onAction(ContributeActions.FromPinDragged(lat, lng))
//                    },
//                    onToPinDragged = { lat, lng ->
//                        onAction(ContributeActions.ToPinDragged(lat, lng))
//                    },
//                )
//            }
//        }
//
//        Spacer(Modifier.height(20.dp))
//
//        StepInputField(
//            label = "TO",
//            value = state.to,
//            placeholder = "e.g., Westlands, Sarit",
//            onValueChange = { onAction(ContributeActions.ToChanged(it)) },
//            errorMessage = state.errors["to"],
//        )
//
//        // Suggestions dropdown for To
//        AnimatedVisibility(
//            visible = state.toSuggestions.isNotEmpty(),
//            enter = fadeIn() + expandVertically(),
//        ) {
//            SuggestionsDropdown(
//                suggestions = state.toSuggestions,
//                onSuggestionClick = { id ->
//                    onAction(ContributeActions.ToSuggestionSelected(id))
//                },
//            )
//        }
//        Spacer(Modifier.height(20.dp))
//
//        StepInputField(
//            label = "ROUTE NO. (optional)",
//            value = state.routeNumber,
//            placeholder = "e.g., 58, 108",
//            onValueChange = { onAction(ContributeActions.RouteNumberChanged(it)) },
//        )
//
//        Spacer(Modifier.height(20.dp))
//
//        Text(
//            text = "SACCOS ON THIS ROUTE (optional)",
//            style = typography.labelSmall,
//            color = colors.onSurfaceVariant,
//        )
//        Spacer(Modifier.height(8.dp))
//
//        state.saccos.forEachIndexed { index, sacco ->
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                MwenyejiInputField(
//                    value = sacco,
//                    onValueChange = { onAction(ContributeActions.SaccoChanged(index, it)) },
//                    placeholder = { Text("e.g., Super Metro") },
//                    modifier =
//                        Modifier
//                            .weight(1f)
//                            .clip(RoundedCornerShape(10.dp)),
//                )
//                if (state.saccos.size > 1) {
//                    IconButton(
//                        onClick = { onAction(ContributeActions.RemoveSacco(index)) },
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Remove",
//                            tint = colors.onSurfaceVariant,
//                        )
//                    }
//                }
//            }
//            Spacer(Modifier.height(8.dp))
//        }
//
//        TextButton(
//            onClick = { onAction(ContributeActions.AddSacco) },
//        ) {
//            Icon(
//                imageVector = Icons.Default.Add,
//                contentDescription = null,
//                modifier = Modifier.size(16.dp),
//            )
//            Spacer(Modifier.width(4.dp))
//            Text("Add another SACCO")
//        }
//        Spacer(Modifier.height(20.dp))
//
//        StepInputField(
//            label = "FARE (KSH)",
//            value = state.fareKsh,
//            placeholder = "e.g., 50",
//            onValueChange = { onAction(ContributeActions.FareChanged(it)) },
//            errorMessage = state.errors["fare"],
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//        )
//    }
// }

@Composable
private fun SuggestionsDropdown(
    suggestions: List<SearchResult>,
    onSuggestionClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(
                    width = 0.5.dp,
                    color = colors.outlineVariant,
                    shape = RoundedCornerShape(8.dp),
                ),
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = suggestion.name,
                    style = typography.bodyMedium,
                    color = colors.onSurface,
                )
                suggestion.fullAddress?.let { address ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = address,
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                suggestion.distanceMeters?.let { meters ->
                    val km = meters / 1000.0
                    Text(
                        text = "%.1f km away".format(km),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            if (index < suggestions.lastIndex) {
                HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun FromMapPreview(
    lat: Double,
    lng: Double,
    displayName: String,
    onPinDragged: (Double, Double) -> Unit, // new — reports updated coords
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography
    LocalContext.current

    val point = Point.fromLngLat(lng, lat)

    val mapViewportState =
        key(lat, lng) {
            rememberMapViewportState {
                setCameraOptions {
                    center(point)
                    zoom(15.0)
                    pitch(0.0)
                }
            }
        }

    Column(modifier = modifier.fillMaxWidth()) {
        MapboxMap(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = "mapbox://styles/mapbox/dark-v11") },
            scaleBar = {},
            compass = {},
            logo = {},
        ) {
            // PointAnnotation supports dragging — Marker does not
            val marker =
                rememberIconImage(
                    key = "mwenyeji-pin",
                    painter = painterResource(id = R.drawable.ic_map_pin), // your pin drawable
                )

            PointAnnotation(point = point) {
                iconImage = marker
                iconSize = 1.2

                // enable dragging
                interactionsState.isDraggable = true

                interactionsState.onDragged { annotation ->
                    val newPoint = annotation.point
                    onPinDragged(newPoint.latitude(), newPoint.longitude())
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = "✓  $displayName",
            style = typography.labelSmall,
            color = colors.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Drag the pin to adjust the exact location",
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
fun StepInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        MwenyejiInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder) },
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = typography.labelSmall,
                color = colors.error,
            )
        }
    }
}
