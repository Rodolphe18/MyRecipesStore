package com.francotte.myrecipesstore.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.ui.compose.categories.CATEGORIES_ROUTE
import com.francotte.myrecipesstore.ui.compose.favorites.FAVORITE_ROUTE
import com.francotte.myrecipesstore.ui.compose.favorites.login.LOGIN_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.HOME_ROUTE
import com.francotte.myrecipesstore.ui.theme.Orange


@Composable
fun RowScope.CustomNavigationBarItem(
    selected: Boolean,
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
        colors = NavigationBarItemColors(Orange, Orange, Orange.copy(alpha = 0.1f), Color.DarkGray, Color.DarkGray, Color.DarkGray, Color.DarkGray),
        alwaysShowLabel = alwaysShowLabel,
    )
}


@Composable
fun BottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            CustomNavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    val icon = if (selected) {
                        destination.selectedIcon
                    } else {
                        destination.unselectedIcon
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        stringResource(destination.titleTextId),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    }
}

sealed class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleTextId: Int,
    val route: String
) {
    data object HOME : TopLevelDestination(
        Icons.Filled.Home,
        Icons.Outlined.Home,
        R.string.home,
        HOME_ROUTE
    )

    data object CATEGORIES : TopLevelDestination(
        Icons.Filled.ShoppingCart,
        Icons.Outlined.ShoppingCart,
        R.string.categories,
        CATEGORIES_ROUTE
    )

    data class FAVORITES(val isAuthenticated: Boolean) : TopLevelDestination(
        Icons.Filled.Favorite,
        Icons.Outlined.FavoriteBorder,
        R.string.favorites,
        if (isAuthenticated) FAVORITE_ROUTE else LOGIN_ROUTE
    )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    return this?.hierarchy?.any { navDestination ->
        navDestination.route == destination.route
    } ?: false
}
