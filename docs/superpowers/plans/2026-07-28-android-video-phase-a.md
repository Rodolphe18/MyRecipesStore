# Intégration vidéo Android (Phase A) — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Attacher une vidéo (galerie) à une recette lors de sa création, l'uploader au backend, et la lire en HLS dans l'écran de détail avec gestion de l'état « en traitement » (polling).

**Architecture:** La vidéo suit le chemin existant de l'image sur les couches network → data → domaine → feature. Nouveauté : un lecteur HLS Media3 (`HlsVideoPlayer` dans `core/designsystem`) et une boucle de polling dans `CustomRecipeDetailViewModel`. Un slot média unique dans l'écran de détail affiche la vidéo prête, sinon un placeholder, sinon l'image.

**Tech Stack:** Kotlin, Compose, Hilt, Retrofit, kotlinx.serialization, AndroidX Media3 (ExoPlayer + HLS), Coroutines.

**Spec de référence :** `docs/superpowers/specs/2026-07-28-android-video-phase-a-design.md`

**Commandes de build (Windows/PowerShell) :** `.\gradlew.bat <tâche>`. Les tests unitaires d'un module : `.\gradlew.bat :core:network:testDebugUnitTest`.

---

## Décisions verrouillées

1. `FavoriteApi.addRecipe` reste `Response<Unit>` (on ajoute seulement la partie `video`). Le statut vidéo s'affiche via `getUserRecipe` → `NetworkCustomRecipe.video`.
2. `toMultiPartBody` est spécifique image (décode/redimensionne le bitmap). Pour la vidéo, un **nouvel** helper `toVideoMultiPartBody` streame le fichier brut (pas de décodage bitmap).
3. Les fichiers HLS sont publics → ExoPlayer lit sans token.
4. Slot média unique : vidéo READY > placeholder (si pas d'image) > image.

## Structure des fichiers

| Module | Fichier | Action |
|---|---|---|
| gradle | `gradle/libs.versions.toml` | alias Media3 |
| core/designsystem | `build.gradle.kts` | dépendances Media3 |
| core/model | `CustomRecipe.kt` | `CustomVideo` + `VideoStatus` + champ `video` |
| core/network | `model/NetworkCustomRecipe.kt` | `NetworkVideo` + champ + mappers |
| core/network | `api/FavoriteApi.kt` | partie `video` |
| core/network | `utils/MultipartExt.kt` | `Uri?.toVideoMultiPartBody` |
| core/data | `favorite/FavoriteManager.kt` | `createRecipe(video)` |
| core/data | `interfaces/FavoritesRepository.kt` | `addCustomRecipe(video)`, `getCustomRecipe` |
| core/data | `repository/FavoritesRepositoryImpl.kt` | impl |
| feature/add_recipe | `AddRecipeViewModel.kt` | `videoUri` state/action/submit |
| feature/add_recipe | `AddRecipeScreen.kt` | bouton galerie vidéo |
| core/designsystem | `component/HlsVideoPlayer.kt` (nouveau) | lecteur Media3 |
| feature/favorites | `CustomRecipeDetailViewModel.kt` | polling + `getCustomRecipe` |
| feature/favorites | `CustomRecipeDetailScreen.kt` | slot média à priorité |

---

## Task 0 : Dépendances Media3

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core/designsystem/build.gradle.kts`

- [ ] **Step 1 : Ajouter la version + les alias au catalogue**

Dans `gradle/libs.versions.toml`, ajouter sous `[versions]` :

```toml
media3 = "1.4.1"
```

Puis, sous `[libraries]` :

```toml
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-exoplayer-hls = { group = "androidx.media3", name = "media3-exoplayer-hls", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
```

- [ ] **Step 2 : Déclarer les dépendances dans designsystem**

Dans `core/designsystem/build.gradle.kts`, dans le bloc `dependencies { }` (après `libs.coil.compose`) :

```kotlin
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
```

- [ ] **Step 3 : Vérifier la résolution des dépendances**

Run: `.\gradlew.bat :core:designsystem:dependencies --configuration debugRuntimeClasspath`
Expected: la sortie liste `androidx.media3:media3-exoplayer-hls:1.4.1` sans erreur.

- [ ] **Step 4 : Commit**

```bash
git add gradle/libs.versions.toml core/designsystem/build.gradle.kts
git commit -m "chore: add Media3 ExoPlayer + HLS dependencies"
```

---

## Task 1 : Modèle domaine (`core/model`)

**Files:**
- Modify: `core/model/src/main/java/com/francotte/model/CustomRecipe.kt`

- [ ] **Step 1 : Ajouter `CustomVideo`, `VideoStatus`, et le champ `video`**

Remplacer le contenu de `CustomRecipe.kt` par :

```kotlin
package com.francotte.model

import androidx.compose.runtime.Immutable

@Immutable
data class CustomRecipe(
    val id: String,
    val title: String,
    val ingredients: List<CustomIngredient>,
    val instructions: String,
    val imageUrl: String?,
    val video: CustomVideo? = null,
)

@Immutable
data class CustomIngredient(
    val name: String,
    val quantity: String,
    val measureType: String,
)

@Immutable
data class CustomVideo(
    val status: VideoStatus,
    val manifestUrl: String?,
    val durationSec: Double?,
)

enum class VideoStatus { PENDING, PROCESSING, READY, FAILED }
```

- [ ] **Step 2 : Compiler le module**

Run: `.\gradlew.bat :core:model:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3 : Commit**

```bash
git add core/model/src/main/java/com/francotte/model/CustomRecipe.kt
git commit -m "feat(model): add video to CustomRecipe"
```

---

## Task 2 : Modèle réseau + mapper (`core/network`) — TDD

**Files:**
- Modify: `core/network/src/main/java/com/francotte/network/model/NetworkCustomRecipe.kt`
- Create: `core/network/src/test/java/com/francotte/network/model/NetworkVideoMapperTest.kt`

- [ ] **Step 1 : Écrire le test du mapper**

Créer `core/network/src/test/java/com/francotte/network/model/NetworkVideoMapperTest.kt` :

```kotlin
package com.francotte.network.model

import com.francotte.model.VideoStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkVideoMapperTest {

    @Test
    fun `maps a known status and fields`() {
        val v = NetworkVideo("id1", "READY", "https://x/master.m3u8", 3.0).asExternalModel()
        assertEquals(VideoStatus.READY, v.status)
        assertEquals("https://x/master.m3u8", v.manifestUrl)
        assertEquals(3.0, v.durationSec!!, 0.001)
    }

    @Test
    fun `unknown status falls back to FAILED`() {
        val v = NetworkVideo("id1", "SOMETHING_ELSE", null, null).asExternalModel()
        assertEquals(VideoStatus.FAILED, v.status)
    }
}
```

- [ ] **Step 2 : Lancer le test → échec (types absents)**

Run: `.\gradlew.bat :core:network:testDebugUnitTest --tests "com.francotte.network.model.NetworkVideoMapperTest"`
Expected: FAIL (compilation — `NetworkVideo` / `asExternalModel` inexistants).

- [ ] **Step 3 : Ajouter `NetworkVideo`, le champ, et les mappers**

Dans `NetworkCustomRecipe.kt`, ajouter les imports :

```kotlin
import com.francotte.model.CustomVideo
import com.francotte.model.VideoStatus
```

Ajouter la data class et son mapper (après `NetworkCustomIngredient`) :

```kotlin
@Serializable
data class NetworkVideo(
    val id: String,
    val status: String,
    val manifestUrl: String?,
    val durationSec: Double?,
)

fun NetworkVideo.asExternalModel(): CustomVideo = CustomVideo(
    status = runCatching { VideoStatus.valueOf(status) }.getOrDefault(VideoStatus.FAILED),
    manifestUrl = manifestUrl,
    durationSec = durationSec,
)
```

Ajouter le champ à `NetworkCustomRecipe` :

```kotlin
@Serializable
data class NetworkCustomRecipe(
    val id: String,
    val title: String,
    val ingredients: List<NetworkCustomIngredient>,
    val instructions: String,
    val imageUrl: String?,
    val video: NetworkVideo? = null,
)
```

Et propager dans `NetworkCustomRecipe.asExternalModel()` : ajouter `video = video?.asExternalModel(),` dans le constructeur de `CustomRecipe`.

- [ ] **Step 4 : Lancer le test → succès**

Run: `.\gradlew.bat :core:network:testDebugUnitTest --tests "com.francotte.network.model.NetworkVideoMapperTest"`
Expected: PASS (2 tests).

- [ ] **Step 5 : Commit**

```bash
git add core/network/src/main/java/com/francotte/network/model/NetworkCustomRecipe.kt core/network/src/test/java/com/francotte/network/model/NetworkVideoMapperTest.kt
git commit -m "feat(network): NetworkVideo model and mapper"
```

---

## Task 3 : Partie vidéo dans l'API (`core/network`)

**Files:**
- Modify: `core/network/src/main/java/com/francotte/network/api/FavoriteApi.kt`

- [ ] **Step 1 : Ajouter `@Part video` à `addRecipe`**

Dans `FavoriteApi.kt`, remplacer la fonction `addRecipe` par :

```kotlin
    @Multipart
    @POST("users/recipes")
    suspend fun addRecipe(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part?,
        @Part video: MultipartBody.Part?,
        @Part("title") title: RequestBody,
        @Part("instructions") instructions: RequestBody,
        @Part("ingredients") ingredients: RequestBody,
    ): Response<Unit>
```

- [ ] **Step 2 : Compiler**

Run: `.\gradlew.bat :core:network:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3 : Commit**

```bash
git add core/network/src/main/java/com/francotte/network/api/FavoriteApi.kt
git commit -m "feat(network): accept video part in addRecipe"
```

---

## Task 4 : Helper multipart vidéo (`core/network`)

**Files:**
- Modify: `core/network/src/main/java/com/francotte/network/utils/MultipartExt.kt`

- [ ] **Step 1 : Ajouter `toVideoMultiPartBody` (stream brut, pas de décodage bitmap)**

Dans `MultipartExt.kt`, ajouter les imports nécessaires (`android.content.Context`, `android.net.Uri`, `okhttp3.MultipartBody`, `okhttp3.MediaType.Companion.toMediaTypeOrNull`, `okhttp3.RequestBody.Companion.asRequestBody`, `java.io.File`, `java.io.FileOutputStream`) puis :

```kotlin
fun Uri?.toVideoMultiPartBody(context: Context): MultipartBody.Part? =
    this?.let { uri ->
        val resolver = context.contentResolver
        val file = File.createTempFile("upload_video", ".mp4", context.cacheDir)
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        } ?: return null
        val requestFile = file.asRequestBody("video/mp4".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("video", file.name, requestFile)
    }
```

- [ ] **Step 2 : Compiler**

Run: `.\gradlew.bat :core:network:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3 : Commit**

```bash
git add core/network/src/main/java/com/francotte/network/utils/MultipartExt.kt
git commit -m "feat(network): raw-streaming video multipart helper"
```

---

## Task 5 : Couche data (`core/data`)

**Files:**
- Modify: `core/data/src/main/java/com/francotte/data/favorite/FavoriteManager.kt`
- Modify: `core/data/src/main/java/com/francotte/data/interfaces/FavoritesRepository.kt`
- Modify: `core/data/src/main/java/com/francotte/data/repository/FavoritesRepositoryImpl.kt`

- [ ] **Step 1 : `FavoriteManager.createRecipe` — ajouter `video` + `getCustomRecipe` mapping**

Dans `FavoriteManager.kt`, ajouter l'import `import com.francotte.network.utils.toVideoMultiPartBody`. Remplacer `createRecipe` par :

```kotlin
    suspend fun createRecipe(
        title: String,
        ingredients: List<NetworkCustomIngredient>,
        instructions: String,
        image: Uri?,
        video: Uri?,
    ): Result<Unit> {
        val token = foodPreferencesDataSource.userData.first().token
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        val videoPart = video.toVideoMultiPartBody(context)
        return try {
            val response = withContext(Dispatchers.IO) {
                api.addRecipe("Bearer $token", imagePart, videoPart, titlePart, instructionsPart, ingredientsBody)
            }
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Server error ${response.code()}"))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
```

- [ ] **Step 2 : Interface `FavoritesRepository`**

Dans `FavoritesRepository.kt`, remplacer `addCustomRecipe` et ajouter `getCustomRecipe` :

```kotlin
    suspend fun addCustomRecipe(
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
        video: Uri?,
    ): Result<Unit>

    suspend fun getCustomRecipe(id: String): Result<CustomRecipe>
```

- [ ] **Step 3 : Implémentation `FavoritesRepositoryImpl`**

Dans `FavoritesRepositoryImpl.kt`, remplacer `addCustomRecipe` et ajouter `getCustomRecipe` :

```kotlin
    override suspend fun addCustomRecipe(
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
        video: Uri?,
    ): Result<Unit> =
        favoriteManager.createRecipe(title, ingredients.map { it.asDto() }, instructions, image, video)
            .also { if (it.isSuccess) customRecipesVersion.update { v -> v + 1 } }

    override suspend fun getCustomRecipe(id: String): Result<CustomRecipe> =
        runCatching { favoriteManager.getUserCustomRecipe(id).asExternalModel() }
```

- [ ] **Step 4 : Compiler**

Run: `.\gradlew.bat :core:data:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL` (les appelants de `addCustomRecipe` en erreur seront corrigés en Task 6/8 — si le module data seul compile, continuer).

- [ ] **Step 5 : Commit**

```bash
git add core/data
git commit -m "feat(data): pass video uri through create; expose getCustomRecipe"
```

---

## Task 6 : feature/add_recipe (galerie)

**Files:**
- Modify: `feature/add_recipe/impl/src/main/java/com/francotte/add_recipe/AddRecipeViewModel.kt`
- Modify: `feature/add_recipe/impl/src/main/java/com/francotte/add_recipe/AddRecipeScreen.kt`

- [ ] **Step 1 : État, action, submit**

Dans `AddRecipeViewModel.kt` : ajouter `val videoUri: Uri? = null` à `AddRecipeState` ; ajouter `data class OnVideoChange(val uri: Uri?) : AddRecipeAction` ; gérer l'action dans `onAction` :

```kotlin
            is AddRecipeAction.OnVideoChange -> _state.update { it.copy(videoUri = action.uri) }
```

Remplacer l'appel dans `submit()` par :

```kotlin
            val result = favoritesRepository.addCustomRecipe(
                form.title, form.ingredients, form.instructions, form.imageUri, form.videoUri,
            )
```

Ajouter `videoUri = null` dans le `resetForm()`.

- [ ] **Step 2 : Bouton galerie vidéo dans l'écran**

Dans `AddRecipeScreen.kt`, après le bloc d'aperçu de l'image (le `state.imageUri?.let { ... }`), ajouter un launcher et une section « Video » :

```kotlin
                    val launcherVideoGallery =
                        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                            onAction(AddRecipeAction.OnVideoChange(uri))
                        }
                    Text(
                        text = "Video",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.height(40.dp),
                        onClick = { launcherVideoGallery.launch("video/*") },
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery", fontSize = 12.sp)
                    }
                    state.videoUri?.let {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Vidéo sélectionnée ✓")
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Retirer",
                                color = Orange,
                                modifier = Modifier.clickable { onAction(AddRecipeAction.OnVideoChange(null)) },
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
```

Ajouter l'import `import androidx.compose.material.icons.filled.Videocam`.

- [ ] **Step 3 : Compiler le module**

Run: `.\gradlew.bat :feature:add_recipe:impl:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4 : Commit**

```bash
git add feature/add_recipe
git commit -m "feat(add_recipe): pick a video from gallery"
```

---

## Task 7 : Lecteur HLS (`core/designsystem`)

**Files:**
- Create: `core/designsystem/src/main/java/com/francotte/designsystem/component/HlsVideoPlayer.kt`

- [ ] **Step 1 : Créer le composable**

Créer `HlsVideoPlayer.kt` :

```kotlin
package com.francotte.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun HlsVideoPlayer(manifestUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(manifestUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(manifestUrl))
            prepare()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) exoPlayer.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }
    AndroidView(
        factory = { PlayerView(it).apply { player = exoPlayer } },
        modifier = modifier,
    )
}
```

- [ ] **Step 2 : Compiler**

Run: `.\gradlew.bat :core:designsystem:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. (Si `PlayerView` ou `MediaItem` sont non résolus, vérifier que Task 0 est bien faite.)

- [ ] **Step 3 : Commit**

```bash
git add core/designsystem/src/main/java/com/francotte/designsystem/component/HlsVideoPlayer.kt
git commit -m "feat(designsystem): HLS video player (Media3, lifecycle-aware)"
```

---

## Task 8 : Détail — polling (TDD) + slot média

**Files:**
- Modify: `feature/favorites/impl/src/main/java/com/francotte/favorites/CustomRecipeDetailViewModel.kt`
- Modify: `feature/favorites/impl/src/main/java/com/francotte/favorites/CustomRecipeDetailScreen.kt`
- Create: `feature/favorites/impl/src/test/java/com/francotte/favorites/CustomRecipeDetailPollingTest.kt`

- [ ] **Step 1 : Écrire le test du polling**

Créer `CustomRecipeDetailPollingTest.kt`. Le faux repo renvoie une recette en `PROCESSING` puis `READY` ; on vérifie que l'état finit `READY` :

```kotlin
package com.francotte.favorites

import android.net.Uri
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import com.francotte.model.CustomVideo
import com.francotte.model.LikeableRecipe
import com.francotte.model.VideoStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomRecipeDetailPollingTest {

    private fun recipe(status: VideoStatus) = CustomRecipe(
        id = "r1", title = "t", ingredients = emptyList(), instructions = "i",
        imageUrl = null, video = CustomVideo(status, if (status == VideoStatus.READY) "https://x/master.m3u8" else null, 3.0),
    )

    private class FakeRepo(private val sequence: List<CustomRecipe>) : FavoritesRepository {
        private var index = 0
        override fun observeUserCustomRecipe(id: String): Flow<Result<CustomRecipe>> =
            flowOf(Result.success(sequence.first()))
        override suspend fun getCustomRecipe(id: String): Result<CustomRecipe> {
            val r = sequence[minOf(index + 1, sequence.lastIndex)]; index++
            return Result.success(r)
        }
        // Membres non utilisés par le test :
        override fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> = flowOf(Result.success(emptyList()))
        override suspend fun refreshFavoritesRecipes() {}
        override fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>> = flowOf(Result.success(emptyList()))
        override suspend fun addCustomRecipe(title: String, ingredients: List<CustomIngredient>, instructions: String, image: Uri?, video: Uri?): Result<Unit> = Result.success(Unit)
        override suspend fun updateCustomRecipe(recipeId: String, title: String, ingredients: List<CustomIngredient>, instructions: String, image: Uri?): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun `polling switches PROCESSING to READY then stops`() = runTest {
        val repo = FakeRepo(listOf(recipe(VideoStatus.PROCESSING), recipe(VideoStatus.READY)))
        val vm = CustomRecipeDetailViewModel("r1", repo)
        advanceUntilIdle()
        assertEquals(VideoStatus.READY, vm.state.value.recipe?.video?.status)
    }
}
```

- [ ] **Step 2 : Lancer → échec (polling absent, constructeur non testable)**

Run: `.\gradlew.bat :feature:favorites:impl:testDebugUnitTest --tests "com.francotte.favorites.CustomRecipeDetailPollingTest"`
Expected: FAIL (le polling n'existe pas / statut reste `PROCESSING`).

- [ ] **Step 3 : Implémenter le polling dans le ViewModel**

Dans `CustomRecipeDetailViewModel.kt`, ajouter les imports `import com.francotte.model.VideoStatus`, `import kotlinx.coroutines.delay`, `import kotlinx.coroutines.isActive`. Dans le bloc `init { }`, après l'abonnement `observeUserCustomRecipe`, démarrer le polling :

```kotlin
        startVideoPollingIfNeeded()
```

Et ajouter la méthode privée :

```kotlin
    private fun startVideoPollingIfNeeded() {
        viewModelScope.launch {
            while (isActive) {
                val status = state.value.recipe?.video?.status
                if (status == null || status == VideoStatus.READY || status == VideoStatus.FAILED) break
                delay(4_000)
                favoritesRepository.getCustomRecipe(recipeId.orEmpty())
                    .getOrNull()?.let { refreshed -> _state.update { it.copy(recipe = refreshed) } }
            }
        }
    }
```

> Note : le `init` s'abonne au flow `observeUserCustomRecipe` (émet une fois) ; le polling démarre en parallèle et lit `state.value.recipe`. L'ordre est correct car `launchIn` met à jour l'état de façon synchrone à la première émission sous `runTest`.

- [ ] **Step 4 : Lancer → succès**

Run: `.\gradlew.bat :feature:favorites:impl:testDebugUnitTest --tests "com.francotte.favorites.CustomRecipeDetailPollingTest"`
Expected: PASS.

- [ ] **Step 5 : Slot média à priorité dans l'écran**

Dans `CustomRecipeDetailScreen.kt`, remplacer le bloc `Image(...)` à l'intérieur du `Card` du haut par la logique de priorité. Ajouter les imports `import com.francotte.designsystem.component.HlsVideoPlayer`, `import com.francotte.model.VideoStatus`, `import androidx.compose.foundation.layout.aspectRatio`, `import androidx.compose.foundation.layout.Box`, `import androidx.compose.material3.CircularProgressIndicator`. Remplacer la construction du `painter` + `Image` par :

```kotlin
                val video = recipe?.video
                val slotModifier = Modifier.fillMaxWidth().aspectRatio(1f)
                when {
                    video?.status == VideoStatus.READY && video.manifestUrl != null ->
                        HlsVideoPlayer(video.manifestUrl!!, slotModifier)

                    video != null &&
                        (video.status == VideoStatus.PENDING || video.status == VideoStatus.PROCESSING) &&
                        recipe.imageUrl.isNullOrBlank() ->
                        Box(slotModifier, contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }

                    else -> {
                        val painter =
                            if (state.isEditing && state.imageUri != null) {
                                rememberAsyncImagePainter(state.imageUri)
                            } else {
                                rememberAsyncImagePainter(imageRequestBuilder(context, recipe?.imageUrl ?: ""))
                            }
                        Image(
                            modifier = slotModifier,
                            painter = painter,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                        )
                    }
                }
```

- [ ] **Step 6 : Compiler le module**

Run: `.\gradlew.bat :feature:favorites:impl:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7 : Commit**

```bash
git add feature/favorites
git commit -m "feat(favorites): HLS playback with processing polling and single media slot"
```

---

## Task 9 : Build complet + vérification manuelle

- [ ] **Step 1 : Assembler l'app**

Run: `.\gradlew.bat :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2 : Vérification manuelle sur appareil/émulateur**

1. Se connecter, aller sur « Add recipe », remplir titre/ingrédients/instructions, **choisir une vidéo dans la galerie** (bouton Gallery), soumettre → snackbar de succès.
2. Ouvrir la recette créée dans le détail → le slot média affiche un **spinner** (si pas d'image) ou l'**image** (si image), avec « en traitement ».
3. Attendre ~10-30 s → le slot **bascule sur le lecteur HLS**, la vidéo se lit.
4. Quitter l'écran → vérifier que le son **s'arrête** (release du lecteur).

---

## Auto-revue (couverture spec ↔ plan)

- **Spec §4 (NetworkVideo + champ + mapper)** → Task 2.
- **Spec §4 (partie video dans addRecipe)** → Task 3 ; helper vidéo → Task 4.
- **Spec §5 (CustomRecipe.video + VideoStatus)** → Task 1.
- **Spec §6 (data : createRecipe/addCustomRecipe video + getCustomRecipe)** → Task 5.
- **Spec §7 (add_recipe : videoUri + bouton galerie)** → Task 6.
- **Spec §8a (HlsVideoPlayer)** → Task 7.
- **Spec §8b (slot média à priorité)** → Task 8, Step 5.
- **Spec §8c (polling)** → Task 8, Steps 1-4.
- **Spec §9 (dépendances + lifecycle)** → Task 0 + Task 7.
- **Spec §11 (tests : mapper, polling)** → Tasks 2, 8.

Gaps assumés (hors périmètre Phase A, documentés dans la spec) : garde-fou de taille client (§10) non implémenté ici — à ajouter si souhaité ; différenciation du 413 laissée en message générique ; CameraX / aperçu local / édition = Phases B/C/D.
