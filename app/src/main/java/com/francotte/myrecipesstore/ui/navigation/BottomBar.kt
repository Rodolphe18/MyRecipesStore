package com.francotte.myrecipesstore.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.ui.compose.add_recipe.ADD_ROUTE
import com.francotte.myrecipesstore.ui.compose.categories.CATEGORIES_ROUTE
import com.francotte.myrecipesstore.ui.compose.favorites.FAVORITE_ROUTE
import com.francotte.myrecipesstore.ui.compose.login.LOGIN_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.HOME_ROUTE
import com.francotte.myrecipesstore.ui.compose.search.SEARCH_ROUTE
import com.francotte.myrecipesstore.ui.theme.Orange


@Composable
fun RowScope.CustomNavigationBarItem(
    selected: Boolean,
    selectedIndicatorColor: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        colors = NavigationBarItemColors(
            Orange,
            Orange,
            selectedIndicatorColor,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer,
        ),
        alwaysShowLabel = alwaysShowLabel,
    )
}


@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    isAuthenticated: Boolean
) {
    NavigationBar(modifier = modifier) {
        val customDestinations =
            if (isAuthenticated) destinations.filterNot { it == TopLevelDestination.LOGIN } else destinations.filterNot { it == TopLevelDestination.FAVORITES }
        customDestinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)

            if (destination == TopLevelDestination.ADD) {
                // Custom ADD button (no ripple, full control)
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape)
                            .background(Orange)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigateToDestination(destination)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            } else {
                CustomNavigationBarItem(
                    selected = selected,
                    selectedIndicatorColor = Orange.copy(0.1f),
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            stringResource(destination.titleTextId),
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                )
            }
        }
    }
}


sealed class TopLevelDestination(
    val icon: ImageVector,
    val titleTextId: Int,
    val route: String
) {
    data object HOME : TopLevelDestination(
        Icons.Filled.Home,
        R.string.home,
        HOME_ROUTE
    )

    data object CATEGORIES : TopLevelDestination(
        Icons.Filled.ShoppingCart,
        R.string.categories,
        CATEGORIES_ROUTE
    )

    data object ADD : TopLevelDestination(
        Icons.Filled.Add,
        R.string.add,
        ADD_ROUTE
    )

    data object SEARCH : TopLevelDestination(
        Icons.Filled.Search,
        R.string.search,
        SEARCH_ROUTE
    )

    data object FAVORITES : TopLevelDestination(
        Icons.Filled.Favorite,
        R.string.favorites,
        FAVORITE_ROUTE
    )

    data object LOGIN : TopLevelDestination(
        Icons.Filled.Lock,
        R.string.login,
        LOGIN_ROUTE
    )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    return this?.hierarchy?.any { navDestination ->
        navDestination.route == destination.route
    } ?: false
}
