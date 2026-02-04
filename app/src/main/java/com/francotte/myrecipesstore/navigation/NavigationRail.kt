package com.francotte.myrecipesstore.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.francotte.designsystem.theme.Orange
import com.francotte.navigation.NavigationState
import com.francotte.ui.DeviceMode

@Composable
fun AppNavigationRail(
    modifier: Modifier = Modifier,
    navigationState: NavigationState,
    onNavigateToDestination: (NavKey) -> Unit,
    isAuthenticated: Boolean,
) {
    NavigationRail(modifier = modifier.widthIn(min = 72.dp, max = 96.dp)) {
        val currentNavItems =
            if (isAuthenticated) {
                TOP_LEVEL_NAV_ITEMS.filterNot { it.value == LOGIN }
            } else {
                TOP_LEVEL_NAV_ITEMS.filterNot { it.value == FAVORITES }
            }

        currentNavItems.forEach { (navKey, navItem) ->
            val selected = navKey == navigationState.currentTopLevelKey

            if (navItem == ADD) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Orange)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToDestination(navKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = navItem.selectedIcon,
                            contentDescription = stringResource(navItem.titleTextId),
                            tint = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            } else {
                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(navKey) },
                    icon = {
                        Icon(
                            imageVector = if (selected) navItem.selectedIcon else navItem.unselectedIcon,
                            contentDescription = stringResource(navItem.titleTextId)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(navItem.titleTextId),
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }
    }
}

fun DeviceMode.useNavigationRail(): Boolean {
    return this == DeviceMode.PhoneLandscape || this == DeviceMode.TabletLandscape
}
