package com.francotte.home

import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface LatestRecipes {
    data object Loading : LatestRecipes
    data object Error : LatestRecipes
    data class Success(val latestRecipes: List<LikeableRecipe>) : LatestRecipes
}

sealed interface AmericanRecipes {
    data object Loading : AmericanRecipes
    data object Error : AmericanRecipes
    data class Success(val americanRecipes: List<LikeableRecipe>) : AmericanRecipes
}

sealed interface AreasRecipes {
    data object Loading : AreasRecipes
    data object Error : AreasRecipes
    data class Success(val areasRecipes: Map<String, List<LikeableRecipe>>) : AreasRecipes
}

sealed interface EnglishRecipes {
    data object Loading : EnglishRecipes
    data object Error : EnglishRecipes
    data class Success(val englishRecipes: List<LikeableRecipe>) : EnglishRecipes
}

internal fun Flow<Result<List<LikeableRecipe>>>.mapToLatestRecipes() = map {
    if (it.isSuccess) LatestRecipes.Success(it.getOrDefault(emptyList()))
    else LatestRecipes.Error
}

internal fun Flow<Result<List<LikeableRecipe>>>.mapToAmericanRecipes() = map {
    if (it.isSuccess) AmericanRecipes.Success(it.getOrDefault(emptyList()))
    else AmericanRecipes.Error
}

internal fun Flow<Result<List<LikeableRecipe>>>.mapToEnglishRecipes() = map {
    if (it.isSuccess) EnglishRecipes.Success(it.getOrDefault(emptyList()))
    else EnglishRecipes.Error
}

internal fun Flow<Result<Map<String, List<LikeableRecipe>>>>.mapToAreasRecipes() = map {
    if (it.isSuccess) AreasRecipes.Success(it.getOrDefault(emptyMap()))
    else AreasRecipes.Error
}