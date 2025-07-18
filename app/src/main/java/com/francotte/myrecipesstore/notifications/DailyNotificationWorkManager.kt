package com.francotte.myrecipesstore.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.network.model.NetworkRecipe
import com.francotte.myrecipesstore.network.model.asExternalModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyNotificationWorkManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: RecipeApi,
    private val notifier: Notifier
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val recipe = api.getRandomMeal()
            notifier.postRecipeNotification((recipe.meals.first() as NetworkRecipe).asExternalModel())
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}