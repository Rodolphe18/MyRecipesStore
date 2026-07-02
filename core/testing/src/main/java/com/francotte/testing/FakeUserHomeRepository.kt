package com.francotte.testing

import com.francotte.common.utils.DataResult
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeUserHomeRepository : UserHomeRepository {
    private val latestFlow =
        MutableSharedFlow<List<LikeableRecipe>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val englishFlow =
        MutableSharedFlow<List<LikeableRecipe>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val americanFlow =
        MutableSharedFlow<List<LikeableRecipe>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val areasSectionsFlow =
        MutableSharedFlow<Map<String, List<LikeableRecipe>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val sectionFlows =
        mutableMapOf<String, MutableSharedFlow<List<LikeableRecipe>>>()

    private val categoryFlows =
        mutableMapOf<String, MutableSharedFlow<List<LikeableRecipe>>>()

    override fun observeLatestRecipes(): Flow<List<LikeableRecipe>> = latestFlow
    override suspend fun refreshLatestRecipes(force: Boolean): String? {
        TODO("Not yet implemented")
    }

    override fun observeEnglishAreaRecipes(): Flow<List<LikeableRecipe>> = englishFlow

    override fun observeJapaneseAreaRecipes(): Flow<List<LikeableRecipe>> = americanFlow

    override fun observeFoodAreaSections(): Flow<Map<String, List<LikeableRecipe>>> = areasSectionsFlow
    override suspend fun refreshSpecificFoodAreaSection(
        area: String,
        force: Boolean
    ): String? {
        TODO("Not yet implemented")
    }

    override suspend fun refreshMultipleFoodAreaSection(force: Boolean): Boolean {
        TODO("Not yet implemented")
    }


    override fun observeFoodAreaSection(sectionName: String): Flow<List<LikeableRecipe>> = sectionFlow(sectionName)
    override suspend fun refreshRecipesByCategory(
        category: String,
        force: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun observeRecipesByCategory(category: String): Flow<List<LikeableRecipe>> = categoryFlow(category)

    override suspend fun getRecipesByCategory(category: String): DataResult<List<LikeableRecipe>> {
        TODO("Not yet implemented")
    }

    fun sendLatestRecipes(result: List<LikeableRecipe>) {
        latestFlow.tryEmit(result)
    }

    fun sendEnglishRecipes(result: List<LikeableRecipe>) {
        englishFlow.tryEmit(result)
    }

    fun sendAmericanRecipes(result: List<LikeableRecipe>) {
        americanFlow.tryEmit(result)
    }

    fun sendAreasSections(result: Map<String, List<LikeableRecipe>>) {
        areasSectionsFlow.tryEmit(result)
    }

    fun sendSection(
        sectionName: String,
        result: List<LikeableRecipe>,
    ) {
        sectionFlow(sectionName).tryEmit(result)
    }

    fun sendCategory(
        category: String,
        result: List<LikeableRecipe>,
    ) {
        categoryFlow(category).tryEmit(result)
    }

    private fun sectionFlow(sectionName: String): MutableSharedFlow<List<LikeableRecipe>> =
        sectionFlows.getOrPut(sectionName) {
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }

    private fun categoryFlow(category: String): MutableSharedFlow<List<LikeableRecipe>> =
        categoryFlows.getOrPut(category) {
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }
}
