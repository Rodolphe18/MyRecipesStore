package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class DetailRecipeNavKey(
    val ids: List<String>?,
    val index: Int?,
    val title: String?,
) : NavKey

fun Navigator.navigateToDetail(ids: List<String>,
                               index: Int,
                               title: String) {
    navigate(DetailRecipeNavKey(ids,index,title))
}
