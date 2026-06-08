package com.francotte.myrecipesstore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Activity
import com.francotte.auth.AuthEvent
import com.francotte.auth.SessionRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.data.favorite.ToggleFavoriteResult
import com.francotte.data.interfaces.FavoriteHelper
import com.francotte.inapp_rating.InAppRatingRepository
import com.francotte.model.LikeableRecipe
import com.francotte.ads.BannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainEffect {
    data class ShowSnackBar(val message: String) : MainEffect
    data object NavigateToLogin : MainEffect
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val favoriteHelper: FavoriteHelper,
    private val sessionRepository: SessionRepository,
    private val inAppRatingRepository: InAppRatingRepository,
    private val bannerRepository: BannerRepository,
) : ViewModel() {

    private val _effects = MutableSharedFlow<MainEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<MainEffect> = _effects.asSharedFlow()

    val userImage: StateFlow<String> = userDataRepository.userData
        .map { it.image }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isAuthenticated: StateFlow<Boolean> = sessionRepository.isAuthenticated
    val shouldShowBanners: StateFlow<Boolean> = bannerRepository.shouldShowBanners

    init {
        viewModelScope.launch { userDataRepository.incrementLaunchCount() }
        viewModelScope.launch {
            sessionRepository.authEvents.collect { event ->
                val message = when (event) {
                    is AuthEvent.LoginSuccess    -> "Welcome back ${event.username}"
                    is AuthEvent.RegisterSuccess -> "Welcome ${event.username}! Your account has been created successfully"
                    AuthEvent.UpdateSuccess      -> "Your account has been updated successfully"
                    AuthEvent.PayloadTooLarge    -> "Payload Too Large"
                    AuthEvent.UserAlreadyExists  -> "Your account can't be created: user already exists"
                    AuthEvent.LoginFailed        -> "Email/Password combination failed!"
                    AuthEvent.RegisterFailed     -> "Unknown error. Retry later"
                    AuthEvent.AccountDeleted     -> "Your account has been deleted successfully"
                    is AuthEvent.Disconnected    -> if (event.wasConnected) "You have been disconnected" else "You are not connected"
                }
                _effects.emit(MainEffect.ShowSnackBar(message))
            }
        }
    }

    suspend fun showInAppReviewIfNeeded(activity: Activity) {
        if (inAppRatingRepository.shouldTryToShowInAppReview()) {
            inAppRatingRepository.launchInAppReview(activity)
            inAppRatingRepository.setHasBeenRatedOrNotAskAgainToTrue()
        }
    }

    suspend fun logout() = sessionRepository.logout()

    suspend fun deleteAccount() = sessionRepository.deleteAccount()

    fun toggleFavorite(recipe: LikeableRecipe) {
        viewModelScope.launch {
            when (val result = favoriteHelper.toggleRecipeFavorite(recipe)) {
                is ToggleFavoriteResult.Success -> _effects.emit(
                    MainEffect.ShowSnackBar(if (result.added) "Recipe added to favorites" else "Recipe removed from favorites")
                )
                ToggleFavoriteResult.Offline -> _effects.emit(
                    MainEffect.ShowSnackBar("Offline: favorites will sync when back online")
                )
                ToggleFavoriteResult.Unauthenticated -> _effects.emit(MainEffect.NavigateToLogin)
            }
        }
    }
}
