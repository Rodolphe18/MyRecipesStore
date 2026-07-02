package com.francotte.myrecipesstore.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.francotte.api.AddRecipeNavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.FavoritesNavKey
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.feature.login.api.LoginNavKey
import com.francotte.feature.search.api.SearchNavKey
import com.francotte.myrecipesstore.R
import com.francotte.designsystem.R as DesignR


data class TopLevelNavItem(
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unselectedIcon: Int,
    @param:StringRes val iconTextId: Int,
    @param:StringRes val titleTextId: Int,
)

val HOME = TopLevelNavItem(
    selectedIcon = DesignR.drawable.ic_home_filled,
    unselectedIcon = DesignR.drawable.ic_home,
    R.string.home,
    R.string.home,
)

val CATEGORIES = TopLevelNavItem(
    selectedIcon = DesignR.drawable.ic_categories_filled,
    unselectedIcon = DesignR.drawable.ic_categories,
    R.string.categories,
    R.string.categories,
)

val ADD = TopLevelNavItem(
    selectedIcon = DesignR.drawable.ic_add,
    unselectedIcon = DesignR.drawable.ic_add,
    R.string.add,
    R.string.add,
)

val SEARCH = TopLevelNavItem(
    selectedIcon = DesignR.drawable.ic_search_filled,
    unselectedIcon = DesignR.drawable.ic_search,
    R.string.search,
    R.string.search,
)

val FAVORITES = TopLevelNavItem(
    selectedIcon = DesignR.drawable.ic_favorites_filled,
    unselectedIcon = DesignR.drawable.ic_favorites,
    R.string.favorites,
    R.string.favorites
)

val LOGIN = TopLevelNavItem(
    selectedIcon = DesignR.drawable.ic_login_filled,
    unselectedIcon = DesignR.drawable.ic_login,
    R.string.login,
    R.string.login
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    HomeNavKey to HOME,
    CategoriesNavKey to CATEGORIES,
    AddRecipeNavKey to ADD,
    SearchNavKey to SEARCH,
    FavoritesNavKey to FAVORITES,
    LoginNavKey to LOGIN
)
