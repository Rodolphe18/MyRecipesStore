package com.francotte.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.designsystem.theme.Orange
import com.francotte.model.FavoriteState
import kotlinx.coroutines.delay

@Composable
fun FavButton(
    modifier: Modifier = Modifier,
    syncState: FavoriteState,
    onToggleFavorite: () -> Unit,
    pendingDelayMs: Long = 250L,
) {
    val mode = rememberDeviceMode()
    val dimension = remember(mode) { favButtonDimension(mode) }

    var displayState by remember { mutableStateOf(syncState) }

    LaunchedEffect(syncState, pendingDelayMs) {
        when (syncState) {
            FavoriteState.PendingAdd, FavoriteState.PendingRemove -> {
                delay(pendingDelayMs)
                // si c'est toujours pending après le délai, on l'affiche
                if (syncState == FavoriteState.PendingAdd || syncState == FavoriteState.PendingRemove) {
                    displayState = syncState
                }
            }
            else -> displayState = syncState
        }
    }

    // Couleurs "normales" selon l'état
    val transition = updateTransition(label = "favoriteSync", targetState = displayState)

    val staticBackgroundColor by transition.animateColor(label = "backgroundColor") { state ->
        when (state) {
            FavoriteState.FavoriteSynced -> colorResource(R.color.orange)
            FavoriteState.PendingAdd,
            FavoriteState.PendingRemove,
            FavoriteState.NotFavorite -> MaterialTheme.colorScheme.tertiary
        }
    }

    val iconColor by transition.animateColor(label = "iconColor") { state ->
        when (state) {
            FavoriteState.FavoriteSynced -> Color.White
            FavoriteState.PendingAdd,
            FavoriteState.PendingRemove,
            FavoriteState.NotFavorite -> MaterialTheme.colorScheme.onTertiary
        }
    }

    // ✅ Pulse uniquement quand displayState est pending (et donc après le délai)
    val infinite = rememberInfiniteTransition(label = "pendingPulse")
    val pulsingOrange by infinite.animateColor(
        label = "pulsingOrange",
        initialValue = colorResource(R.color.yellow),
        targetValue = colorResource(R.color.light_orange),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val backgroundColor =
        if (displayState == FavoriteState.PendingAdd || displayState == FavoriteState.PendingRemove) {
            pulsingOrange
        } else {
            staticBackgroundColor
        }

    val checked =
        displayState == FavoriteState.FavoriteSynced || displayState == FavoriteState.PendingAdd

    Box(
        modifier =
            modifier
                .size(dimension.buttonSize)
                .background(backgroundColor, CircleShape)
                .clip(CircleShape)
                .toggleable(
                    value = checked,
                    onValueChange = { onToggleFavorite() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(dimension.iconSize),
            tint = iconColor,
        )
    }
}



@Immutable
data class FavButtonDimension(
    val buttonSize: Dp,
    val iconSize: Dp,
)

fun favButtonDimension(mode: DeviceMode): FavButtonDimension = when (mode) {
    DeviceMode.PhonePortrait -> FavButtonDimension(buttonSize = 45.dp, iconSize = 25.dp)
    DeviceMode.PhoneLandscape -> FavButtonDimension(buttonSize = 35.dp, iconSize = 20.dp)
    DeviceMode.TabletPortrait -> FavButtonDimension(buttonSize = 50.dp, iconSize = 30.dp)
    DeviceMode.TabletLandscape -> FavButtonDimension(buttonSize = 55.dp, iconSize = 35.dp)
}
