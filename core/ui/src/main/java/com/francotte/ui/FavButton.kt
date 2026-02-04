package com.francotte.ui

import androidx.compose.animation.animateColor
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FavButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    val mode = LocalAppLayout.current.mode
    val dimension = remember(mode) { favButtonDimension(mode) }
    val transition = updateTransition(label = "favorite", targetState = isFavorite)
    val backgroundColor by transition.animateColor(
        label = "backgroundColor",
    ) { isFav -> if (isFav) colorResource(R.color.orange) else MaterialTheme.colorScheme.tertiary }
    val iconColor by transition.animateColor(
        label = "iconColor",
    ) { isFav -> if (isFav) Color.White else MaterialTheme.colorScheme.onTertiary }

    Box(
        modifier =
            modifier
                .size(dimension.buttonSize)
                .background(backgroundColor, CircleShape)
                .clip(CircleShape)
                .toggleable(
                    value = isFavorite,
                    onValueChange = { onToggleFavorite() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(dimension.iconSize),
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
