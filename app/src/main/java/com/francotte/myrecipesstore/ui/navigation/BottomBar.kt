package com.francotte.myrecipesstore.ui.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.francotte.myrecipesstore.ui.compose.favorites.login.LOGIN_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.HOME_ROUTE
import com.francotte.myrecipesstore.ui.compose.search.SEARCH_ROUTE
import com.francotte.myrecipesstore.ui.theme.Orange


@Composable
fun RowScope.CustomNavigationBarItem(
    selected: Boolean,
    selectedIndicatorColor:Color,
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
            Color.DarkGray,
            Color.DarkGray,
            Color.DarkGray,
            Color.DarkGray
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
    isAuthenticated:Boolean
) {
    NavigationBar(modifier = modifier) {
        val customDestinations = if (isAuthenticated) destinations.filterNot { it == TopLevelDestination.LOGIN } else destinations.filterNot { it == TopLevelDestination.FAVORITES }
        customDestinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            CustomNavigationBarItem(
                selected = selected,
                selectedIndicatorColor = if (destination == TopLevelDestination.ADD) Color.Transparent else Orange.copy(0.1f),
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    if (destination == TopLevelDestination.ADD) {
                        Box(Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.Red)) {
                            Icon(Icons.Filled.Add, null, Modifier.align(Alignment.Center), Color.White)
                        }

                    } else {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    if (destination != TopLevelDestination.ADD) {
                        Text(
                            stringResource(destination.titleTextId),
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
            )
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
        LOGIN_ROUTE)
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    return this?.hierarchy?.any { navDestination ->
        navDestination.route == destination.route
    } ?: false
}
