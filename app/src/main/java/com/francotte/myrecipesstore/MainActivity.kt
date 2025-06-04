package com.francotte.myrecipesstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.myrecipesstore.auth.AuthManager
import com.francotte.myrecipesstore.favorites.FavoriteManager
import com.francotte.myrecipesstore.ui.navigation.rememberAppState
import com.francotte.myrecipesstore.ui.theme.MyRecipesStoreTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var favoriteManager: FavoriteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isAuthenticated by authManager.isAuthenticated.collectAsStateWithLifecycle()
            val state = rememberAppState(favoriteManager = favoriteManager, isAuthenticated = isAuthenticated)
            val scope = rememberCoroutineScope()
            var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

            MyRecipesStoreTheme {
                FoodApp(showSettingsDialog = showSettingsDialog, onSettingsClick = { showSettingsDialog = true }, onSettingsDismissed = { showSettingsDialog =false },  onToggleFavorite = { scope.launch { favoriteManager.toggleRecipeFavorite(it) }  },  appState = state)
            }
        }
    }
}
