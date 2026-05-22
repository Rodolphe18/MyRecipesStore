package com.francotte.home.di

import com.francotte.home.delegate.AreasRecipesDelegate
import com.francotte.home.delegate.AreasRecipesDelegateImpl
import com.francotte.home.delegate.EnglishRecipesDelegate
import com.francotte.home.delegate.EnglishRecipesDelegateImpl
import com.francotte.home.delegate.JapaneseRecipesDelegate
import com.francotte.home.delegate.JapaneseRecipesDelegateImpl
import com.francotte.home.delegate.LatestRecipesDelegate
import com.francotte.home.delegate.LatestRecipesDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class HomeDelegateModule {

    @Binds
    abstract fun bindLatestRecipesDelegate(
        impl: LatestRecipesDelegateImpl
    ): LatestRecipesDelegate

    @Binds
    abstract fun bindAmericanRecipesDelegate(
        impl: JapaneseRecipesDelegateImpl
    ): JapaneseRecipesDelegate

    @Binds
    abstract fun bindAreasRecipesDelegate(
        impl: AreasRecipesDelegateImpl
    ): AreasRecipesDelegate

    @Binds
    abstract fun bindEnglishRecipesDelegate(
        impl: EnglishRecipesDelegateImpl
    ): EnglishRecipesDelegate
}
