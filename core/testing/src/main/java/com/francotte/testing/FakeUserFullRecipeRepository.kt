package com.francotte.testing

import com.francotte.data.interfaces.UserFullRecipeRepository
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeUserFullRecipeRepository : UserFullRecipeRepository {

    private val flows = mutableMapOf<Long, MutableSharedFlow<Result<LikeableRecipe>>>()

    private fun flowFor(id: Long) = flows.getOrPut(id) {
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    override fun observeFullRecipe(id: Long): Flow<Result<LikeableRecipe>> = flowFor(id)

    /** Pousse une recette pour [id]. replay=1 → rejouée à la souscription du VM. */
    fun emit(id: Long, recipe: LikeableRecipe) {
        flowFor(id).tryEmit(Result.success(recipe))
    }
}
