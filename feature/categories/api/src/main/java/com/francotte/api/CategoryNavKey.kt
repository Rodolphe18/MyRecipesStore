package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class CategoryNavKey(val category: String) : NavKey


fun Navigator.navigateToCategory(category: String) {
    navigate(CategoryNavKey(category))
}
