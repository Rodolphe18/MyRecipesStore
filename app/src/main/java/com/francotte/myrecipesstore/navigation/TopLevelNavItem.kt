package com.francotte.myrecipesstore.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.francotte.api.AddRecipeNavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.FavoritesNavKey
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.feature.login.api.LoginNavKey
import com.francotte.feature.search.api.SearchNavKey
import com.francotte.myrecipesstore.R


data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
)

val HOME = TopLevelNavItem(
   selectedIcon= Icons.Filled.Home,
    unselectedIcon =Icons.Filled.Lock,
    R.string.home,
    R.string.home,
)

val CATEGORIES = TopLevelNavItem(
    selectedIcon= Icons.Filled.ShoppingCart,
    unselectedIcon =Icons.Filled.Lock,
    R.string.categories,
    R.string.categories,
)

val ADD = TopLevelNavItem(
    selectedIcon= Icons.Filled.Add,
    unselectedIcon =Icons.Filled.Lock,
    R.string.add,
    R.string.add,
)

val SEARCH = TopLevelNavItem(
    selectedIcon= Icons.Filled.Search,
    unselectedIcon =Icons.Filled.Lock,
    R.string.search,
    R.string.search,
)

val FAVORITES = TopLevelNavItem(
    selectedIcon=Icons.Filled.Favorite,
    unselectedIcon =Icons.Filled.Lock,
    R.string.favorites,
    R.string.favorites
)

val LOGIN = TopLevelNavItem(
    selectedIcon=Icons.Filled.Lock,
    unselectedIcon =Icons.Filled.Lock,
    R.string.login,
    R.string.login
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    HomeNavKey to HOME,
    CategoriesNavKey to CATEGORIES,
    AddRecipeNavKey to ADD,
    SearchNavKey to SEARCH,
    FavoritesNavKey to FAVORITES,
    LoginNavKey to LOGIN,
)
