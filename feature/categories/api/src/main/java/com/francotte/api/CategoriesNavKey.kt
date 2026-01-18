package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object CategoriesNavKey : NavKey


fun Navigator.navigateToCategories() {
    navigate(CategoriesNavKey)
}
