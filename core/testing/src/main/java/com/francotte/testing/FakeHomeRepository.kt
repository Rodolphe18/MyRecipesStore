package com.francotte.testing

import com.francotte.data.repository.HomeRepository
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeHomeRepository: HomeRepository {

    private val latestFlow =
        MutableSharedFlow<Result<List<LikeableRecipe>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val englishFlow =
        MutableSharedFlow<Result<List<LikeableRecipe>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val americanFlow =
        MutableSharedFlow<Result<List<LikeableRecipe>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val areasSectionsFlow =
        MutableSharedFlow<Result<Map<String, List<LikeableRecipe>>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val sectionFlows =
        mutableMapOf<String, MutableSharedFlow<Result<List<LikeableRecipe>>>>()

    private val categoryFlows =
        mutableMapOf<String, MutableSharedFlow<Result<List<LikeableRecipe>>>>()


    override fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>> = latestFlow

    override fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>> = englishFlow

    override fun observeAmericanAreaRecipes(): Flow<Result<List<LikeableRecipe>>> = americanFlow

    override fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>> = areasSectionsFlow

    override fun observeFoodAreaSection(sectionName: String): Flow<Result<List<LikeableRecipe>>> =
        sectionFlow(sectionName)

    override fun observeRecipesByCategory(category: String): Flow<Result<List<LikeableRecipe>>> =
        categoryFlow(category)

    fun sendLatestRecipes(result: Result<List<LikeableRecipe>>) {
        latestFlow.tryEmit(result)
    }

    fun sendEnglishRecipes(result: Result<List<LikeableRecipe>>) {
        englishFlow.tryEmit(result)
    }

    fun sendAmericanRecipes(result: Result<List<LikeableRecipe>>) {
        americanFlow.tryEmit(result)
    }

    fun sendAreasSections(result: Result<Map<String, List<LikeableRecipe>>>) {
        areasSectionsFlow.tryEmit(result)
    }

    fun sendSection(sectionName: String, result: Result<List<LikeableRecipe>>) {
        sectionFlow(sectionName).tryEmit(result)
    }

    fun sendCategory(category: String, result: Result<List<LikeableRecipe>>) {
        categoryFlow(category).tryEmit(result)
    }


    private fun sectionFlow(sectionName: String): MutableSharedFlow<Result<List<LikeableRecipe>>> =
        sectionFlows.getOrPut(sectionName) {
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }

    private fun categoryFlow(category: String): MutableSharedFlow<Result<List<LikeableRecipe>>> =
        categoryFlows.getOrPut(category) {
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }


}