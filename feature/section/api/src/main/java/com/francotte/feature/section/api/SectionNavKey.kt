package com.francotte.feature.section.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class SectionNavKey(
    val sectionName: String
) : NavKey

fun Navigator.navigateToSection(sectionName: String) {
    navigate(SectionNavKey(sectionName))
}
