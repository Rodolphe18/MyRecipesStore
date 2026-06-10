package com.francotte.detail

import com.francotte.model.FavoriteState
import com.francotte.model.LikeableRecipe
import com.francotte.model.TestRecipe
import com.francotte.testing.FakeUserFullRecipeRepository
import com.francotte.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetailRecipeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserFullRecipeRepository()

    private fun likeable(id: String, name: String) =
        LikeableRecipe(TestRecipe(idMeal = id, strMeal = name), FavoriteState.NotFavorite)

    @Test
    fun `loads every recipe of the batch eagerly`() = runTest {
        repository.emit(1L, likeable("1", "Alpha"))
        repository.emit(2L, likeable("2", "Beta"))

        val viewModel = DetailRecipeViewModel(
            detailRecipeRepository = repository,
            ids = listOf("1", "2"),
            index = 0,
            recipeTitle = "Alpha",
        )

        val state = viewModel.state.value
        assertEquals(2, state.recipes.size)
        assertEquals("Alpha", state.recipes[0]?.recipe?.strMeal)
        assertEquals("Beta", state.recipes[1]?.recipe?.strMeal)
    }

    @Test
    fun `OnRecipeSelected updates selectedIndex and title`() = runTest {
        repository.emit(1L, likeable("1", "Alpha"))
        repository.emit(2L, likeable("2", "Beta"))
        val viewModel = DetailRecipeViewModel(repository, listOf("1", "2"), 0, "Alpha")

        viewModel.onAction(DetailAction.OnRecipeSelected(1))

        assertEquals(1, viewModel.state.value.selectedIndex)
        assertEquals("Beta", viewModel.state.value.title)
    }
}
