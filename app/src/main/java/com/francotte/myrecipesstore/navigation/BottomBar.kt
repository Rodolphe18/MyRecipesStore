package com.francotte.myrecipesstore.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
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
import com.francotte.designsystem.component.CustomNavigationBarItem
import com.francotte.designsystem.theme.Orange
import com.francotte.navigation.NavigationState

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    navigationState: NavigationState,
    onNavigateToDestination: (NavKey) -> Unit,
    isAuthenticated: Boolean,
) {
    NavigationBar(modifier = modifier) {
        val currentNavItems =
            if (isAuthenticated) {
                TOP_LEVEL_NAV_ITEMS.filterNot { it.value == LOGIN }
            } else {
                TOP_LEVEL_NAV_ITEMS.filterNot { it.value == FAVORITES }
            }
        currentNavItems.forEach { (navKey, navItem) ->
            val selected = navKey == navigationState.currentTopLevelKey

            if (navItem == ADD) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(55.dp)
                                .clip(CircleShape)
                                .background(Orange)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onNavigateToDestination(navKey)
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }
            } else {
                CustomNavigationBarItem(
                    selected = selected,
                    selectedIndicatorColor = Orange.copy(0.1f),
                    onClick = { onNavigateToDestination(navKey) },
                    icon = {
                        Icon(
                            imageVector = navItem.selectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            stringResource(navItem.titleTextId),
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp,
                        )
                    },
                )
            }
        }
    }
}
