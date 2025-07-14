package com.francotte.myrecipesstore.ui.compose.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.ui.compose.add_recipe.bitmapToUri
import com.francotte.myrecipesstore.ui.compose.composables.whiteYellowVerticalGradient
import com.francotte.myrecipesstore.ui.compose.login.LoginViewModel
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.ui.theme.Orange
import com.francotte.myrecipesstore.util.ScreenCounter
import kotlinx.serialization.Serializable

@Serializable
data object ProfileRoute

fun NavController.navigateToProfileScreen(navOptions: NavOptions? = null) {
    this.navigate(ProfileRoute, navOptions)
}

fun NavGraphBuilder.profileScreen(onBackPressed: () -> Unit) {
    composable<ProfileRoute> {
        ProfileRoute(onBackPressed = onBackPressed)
    }
}

@Composable
fun ProfileRoute(onBackPressed: () -> Unit, viewModel: ProfileViewModel= hiltViewModel()) {
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    ProfileScreen(onBackPressed, currentUser) { name, image -> viewModel.setProfile(name, image) }
    ScreenCounter.increment()
}

