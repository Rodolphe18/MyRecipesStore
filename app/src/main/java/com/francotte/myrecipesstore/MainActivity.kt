package com.francotte.myrecipesstore

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.francotte.myrecipesstore.manager.AuthManager
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.permissions.NotificationPermissionEffect
import com.francotte.myrecipesstore.ui.compose.composables.SplashScreen
import com.francotte.myrecipesstore.ui.compose.reset.reset.navigateToResetPasswordScreen
import com.francotte.myrecipesstore.ui.theme.FoodTheme
import com.francotte.myrecipesstore.util.LaunchCounterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var favoriteManager: FavoriteManager

    @Inject
    lateinit var launchCounterManager: LaunchCounterManager

    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)



       // viewModel.loadInterstitial(this)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    enableEdgeToEdge()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setKeepOnScreenCondition {
                false
             //   viewModel.uiState.value.shouldKeepSplashScreen()
            }
        }
        if (viewModel.uiState.value is MainActivityUiState.Success && (viewModel.uiState.value as MainActivityUiState.Success).hasTimedOut) {
            viewModel.retryLoadInterstitial(this)
        }
        setContent {
            val isAuthenticated by authManager.isAuthenticated.collectAsStateWithLifecycle()
            Log.d("debug_is_authenticated", isAuthenticated.toString())
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            Log.d("debug_connected", isAuthenticated.toString())
            val data: Uri? = intent?.data
            val state = rememberAppState(
                favoriteManager = favoriteManager,
                authManager = authManager,
                launchCounterManager = launchCounterManager,
                resetPasswordToken = data?.getQueryParameter("token")
            )

            FoodTheme {
                FoodApp(context = this, appState = state, windowSizeClass = calculateWindowSizeClass(activity = this))
                NotificationPermissionEffect()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && uiState.shouldKeepSplashScreen()) {
                    SplashScreen()
                }

            }
        }
    }
}



