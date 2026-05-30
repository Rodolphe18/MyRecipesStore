package com.francotte.data.repository

import com.francotte.data.interfaces.HomeRepository
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipes
import com.francotte.model.mapToLikeableLightRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class CompositeUserHomeRepository @Inject constructor(
    private val offlineFirstHomeRepository: HomeRepository,
    private val userDataRepository: UserDataRepository,
) : UserHomeRepository {

    override fun observeLatestRecipes(): Flow<List<LikeableRecipe>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeLatestRecipes()
        ) { userData, latest ->
            latest.mapToLikeableFullRecipes(userData)
        }

    override suspend fun refreshLatestRecipes(force: Boolean): String? {
        return offlineFirstHomeRepository.refreshLatestRecipes(force)
    }

    override fun observeEnglishAreaRecipes(): Flow<List<LikeableRecipe>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesListByArea("British"),
        ) { userData, latestRecipes ->
            latestRecipes.mapToLikeableLightRecipes(userData)
        }

    override fun observeJapaneseAreaRecipes(): Flow<List<LikeableRecipe>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesListByArea("Japanese"),
        ) { userData, latestRecipes ->
            latestRecipes.mapToLikeableLightRecipes(userData).take(10)
        }

    override suspend fun refreshMultipleFoodAreaSection(force: Boolean): Boolean {
        enumValues<FoodAreaSection>().forEach { section ->
            offlineFirstHomeRepository.refreshRecipesListByArea(section.title, force)
        }
        return true
    }

    override suspend fun refreshSpecificFoodAreaSection(area: String, force: Boolean): String? {
        return offlineFirstHomeRepository.refreshRecipesListByArea(area, force)
    }


    override fun observeFoodAreaSections(): Flow<Map<String, List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            combine(
                enumValues<FoodAreaSection>().map { section ->
                    offlineFirstHomeRepository
                        .observeRecipesListByArea(section.title)
                        .map { recipeList -> section.title to recipeList }
                },
            ) { it.toMap() },
        ) { userData, recipesMap ->
            recipesMap.mapValues { (_, recipes) ->
                recipes.mapToLikeableLightRecipes(userData)
            }
        }


    override fun observeRecipesByCategory(category: String): Flow<List<LikeableRecipe>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesByCategory(category),
        ) { userData, recipes ->
           val recipesNotNull = recipes ?: emptyList()
            recipesNotNull.mapToLikeableLightRecipes(userData)
        }


    override fun observeFoodAreaSection(sectionName: String): Flow<List<LikeableRecipe>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesListByArea(sectionName),
        ) { userData, recipes ->
            recipes.mapToLikeableLightRecipes(userData)
        }

    override suspend fun refreshRecipesByCategory(
        category: String,
        force: Boolean
    ): Boolean {
        return offlineFirstHomeRepository.refreshRecipesByCategory(category, force)
    }
}



private enum class FoodAreaSection(val title: String) { CHINESE("Chinese"), PORTUGUESE("Portuguese"),URUGUAYAN("Uruguayan") }
