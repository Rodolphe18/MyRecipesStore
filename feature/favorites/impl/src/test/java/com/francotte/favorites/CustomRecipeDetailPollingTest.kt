package com.francotte.favorites

import android.net.Uri
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import com.francotte.model.CustomVideo
import com.francotte.model.LikeableRecipe
import com.francotte.model.VideoStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CustomRecipeDetailPollingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun recipe(status: VideoStatus) = CustomRecipe(
        id = "r1", title = "t", ingredients = emptyList(), instructions = "i", imageUrl = null,
        video = CustomVideo(
            status = status,
            manifestUrl = if (status == VideoStatus.READY) "https://x/master.m3u8" else null,
            durationSec = 3.0,
        ),
    )

    private class FakeRepo(private val sequence: List<CustomRecipe>) : FavoritesRepository {
        private var index = 0
        override fun observeUserCustomRecipe(id: String): Flow<Result<CustomRecipe>> =
            flowOf(Result.success(sequence.first()))
        override suspend fun getCustomRecipe(id: String): Result<CustomRecipe> {
            val r = sequence[minOf(index + 1, sequence.lastIndex)]; index++
            return Result.success(r)
        }
        override fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> = flowOf(Result.success(emptyList()))
        override suspend fun refreshFavoritesRecipes() {}
        override fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>> = flowOf(Result.success(emptyList()))
        override suspend fun addCustomRecipe(title: String, ingredients: List<CustomIngredient>, instructions: String, image: Uri?, video: Uri?): Result<Unit> = Result.success(Unit)
        override suspend fun updateCustomRecipe(recipeId: String, title: String, ingredients: List<CustomIngredient>, instructions: String, image: Uri?): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun `polling switches PROCESSING to READY then stops`() = runTest(mainDispatcherRule.dispatcher.scheduler) {
        val repo = FakeRepo(listOf(recipe(VideoStatus.PROCESSING), recipe(VideoStatus.READY)))
        val vm = CustomRecipeDetailViewModel("r1", repo)
        advanceUntilIdle()
        assertEquals(VideoStatus.READY, vm.state.value.recipe?.video?.status)
    }
}
