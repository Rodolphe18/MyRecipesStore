package com.francotte.myrecipesstore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.myrecipesstore.manager.AuthManager
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.ui.theme.FoodTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var favoriteManager: FavoriteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            val isAuthenticated by authManager.isAuthenticated.collectAsStateWithLifecycle()
            Log.d("debug_connected", isAuthenticated.toString())
            val state = rememberAppState(favoriteManager = favoriteManager, authManager = authManager, isAuthenticated = isAuthenticated)
            val scope = rememberCoroutineScope()
            var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
            var showAddRecipe by rememberSaveable { mutableStateOf(false) }
             FoodTheme {
                FoodApp(context = this, showSettingsDialog = showSettingsDialog, showAddRecipe = showAddRecipe, onNavigationClick = { showAddRecipe = true }, onSettingsClick = { showSettingsDialog = true }, onSettingsDismissed = { showSettingsDialog =false },  onToggleFavorite = { recipe, isChecked -> scope.launch {  favoriteManager.toggleRecipeFavorite(recipe) }  }, appState = state)
            }
        }
    }
}
