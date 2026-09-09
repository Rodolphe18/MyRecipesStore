# Design — Intégration vidéo Android, Phase A

**Date :** 2026-07-28
**Projet :** MyRecipesStore (app Android, clean architecture multi-modules)
**Objectif :** permettre à l'utilisateur d'attacher une vidéo (choisie dans la galerie) à sa
recette lors de la création, de l'uploader au backend, et de la regarder en streaming HLS
adaptatif depuis l'écran de détail — avec gestion de l'état « en cours de traitement ».

**Backend de référence :** le pipeline HLS est déjà en production (upload multipart →
transcodage ffmpeg → `master.m3u8` servi par nginx/Cloudflare). Voir la spec backend
`2026-07-27-video-streaming-hls-design.md`. Cette spec ne concerne QUE le client Android.

---

## 1. Contexte

L'app suit une clean architecture : `FavoriteApi` (réseau, Retrofit) → `FavoriteManager` /
`FavoritesRepository` (data) → `CustomRecipe` (domaine) → `AddRecipeViewModel` /
`CustomRecipeDetailViewModel` (features). La création d'une recette custom passe déjà une
image en multipart ; le champ `imageUri` existe sur toute la chaîne. La vidéo suit
**exactement le même chemin**.

Découvertes de cadrage :
- Le module `feature/video` existant sert la lecture **YouTube** (WebView) des recettes
  TheMealDB — sans rapport avec le HLS. La lecture HLS est donc **nouvelle** (Media3).
- **Media3/ExoPlayer n'est pas encore une dépendance** du projet.
- Le détail d'une recette custom est dans `feature/favorites/impl`
  (`CustomRecipeDetailScreen` + `CustomRecipeDetailViewModel`).

## 2. Périmètre

**Phase A (cette spec)** — le fil de bout en bout minimal :
- source vidéo = **galerie uniquement** (Photo Picker `GetContent("video/*")`) ;
- ajout **à la création** de la recette ;
- lecture **HLS** dans l'écran de détail ;
- gestion du traitement = placeholder « en traitement » + **polling** jusqu'à `READY`.

**Hors périmètre Phase A** (phases ultérieures) :
- **Phase B** : enregistrement in-app via **CameraX** (2ᵉ source).
- **Phase C** : **aperçu local** immédiat puis bascule vers HLS.
- **Phase D** : **édition** — ajouter/remplacer la vidéo d'une recette existante.
- Miniature vidéo statique (`coil-video`).

## 3. Décisions

1. La vidéo suit le chemin de l'image (`imageUri` → `videoUri`) sur les 3 couches.
2. En Phase A, `FavoriteApi.addRecipe` **reste `Response<Unit>`** (on ajoute juste la partie
   `video`). Le statut vidéo s'affiche via `getUserRecipe` → `NetworkCustomRecipe.video`. Le
   corps de retour d'`addRecipe` n'est nécessaire qu'en Phase C (aperçu local).
3. Les fichiers HLS sont **publics** (nginx/Cloudflare, sans auth) → ExoPlayer lit sans token.
   Seuls les appels d'API portent le Bearer token.
4. Le lecteur HLS est un **composable réutilisable** dans `core/designsystem`, à côté de
   `YouTubeWebViewPlayer`.

## 4. Modèles & réseau (`core/network`)

`NetworkCustomRecipe.kt` gagne un objet vidéo reflétant le `VideoResponse` backend :

```kotlin
@Serializable
data class NetworkVideo(
    val id: String,
    val status: String,          // "PENDING" / "PROCESSING" / "READY" / "FAILED"
    val manifestUrl: String?,    // master.m3u8, non-null si READY
    val durationSec: Double?
)

@Serializable
data class NetworkCustomRecipe(
    val id: String,
    val title: String,
    val ingredients: List<NetworkCustomIngredient>,
    val instructions: String,
    val imageUrl: String?,
    val video: NetworkVideo? = null      // ← nouveau
)
```

`FavoriteApi.addRecipe` gagne la partie vidéo (retour inchangé en Phase A) :

```kotlin
@Multipart
@POST("users/recipes")
suspend fun addRecipe(
    @Header("Authorization") token: String,
    @Part image: MultipartBody.Part?,
    @Part video: MultipartBody.Part?,          // ← nouveau
    @Part("title") title: RequestBody,
    @Part("instructions") instructions: RequestBody,
    @Part("ingredients") ingredients: RequestBody,
): Response<Unit>
```

Le helper `uriToMultipart(uri, context, partName)` existe déjà et est générique — réutilisé
avec `partName = "video"` (type `video/*`). Le mapper `NetworkCustomRecipe.asExternalModel()`
transporte le nouveau champ `video` vers le domaine.

## 5. Domaine (`core/model`)

```kotlin
@Immutable
data class CustomRecipe(
    val id: String,
    val title: String,
    val ingredients: List<CustomIngredient>,
    val instructions: String,
    val imageUrl: String?,
    val video: CustomVideo? = null           // ← nouveau
)

@Immutable
data class CustomVideo(
    val status: VideoStatus,
    val manifestUrl: String?,
    val durationSec: Double?
)

enum class VideoStatus { PENDING, PROCESSING, READY, FAILED }
```

Mapper `NetworkVideo → CustomVideo` : conversion du `status: String` en `VideoStatus`
(valeur inconnue → `FAILED` par prudence).

## 6. Data (`core/data`)

Le `videoUri` traverse la data comme l'image :

```kotlin
// FavoriteManager
suspend fun createRecipe(
    title: String, ingredients: List<NetworkCustomIngredient>,
    instructions: String, image: Uri?, video: Uri?          // ← nouveau
): Result<Unit> {
    val imagePart = uriToMultipart(image, context, "image")
    val videoPart = uriToMultipart(video, context, "video")  // ← réutilise le helper
    // ... favoriteApi.addRecipe(token, imagePart, videoPart, title, instructions, ingredients)
}

// FavoritesRepository (interface + impl)
suspend fun addCustomRecipe(
    title: String, ingredients: List<CustomIngredient>,
    instructions: String, image: Uri?, video: Uri?           // ← nouveau
): Result<Unit>

// Nouveau : re-fetch ponctuel pour le polling (expose favoriteManager.getUserCustomRecipe)
suspend fun getCustomRecipe(id: String): Result<CustomRecipe>
```

## 7. feature/add_recipe (galerie)

État & action (duplication de `imageUri`) :

```kotlin
data class AddRecipeState(..., val imageUri: Uri? = null, val videoUri: Uri? = null)

sealed interface AddRecipeAction {
    ...
    data class OnVideoChange(val uri: Uri?) : AddRecipeAction
}
```

Le ViewModel gère `OnVideoChange` ; `submit()` passe `form.videoUri` à `addCustomRecipe`. La
vidéo est **optionnelle** (`canSubmit` inchangé).

UI — une section « Video » calquée sur la section « Images », avec en Phase A **un seul
bouton (Gallery)** (le bouton « Record » CameraX viendra en Phase B) :

```kotlin
val launcherVideoGallery =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onAction(AddRecipeAction.OnVideoChange(uri))
    }
Button(onClick = { launcherVideoGallery.launch("video/*") }) { Text("Gallery") }
state.videoUri?.let { Text("Vidéo sélectionnée ✓") /* + bouton retirer → OnVideoChange(null) */ }
```

Pas d'aperçu vidéo miniature en Phase A (juste « sélectionnée ✓ » + retirer).

## 8. Lecture HLS + polling (`feature/favorites` + `core/designsystem`)

### a) Lecteur HLS réutilisable (`core/designsystem`)

```kotlin
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
            exoPlayer.release()   // évite fuite mémoire + son persistant
        }
    }
    AndroidView(factory = { PlayerView(it).apply { player = exoPlayer } }, modifier = modifier)
}
```

### b) Emplacement média unique dans `CustomRecipeDetailScreen`

La vidéo **n'est pas une section séparée** : elle occupe **le même slot média** que l'image
(le `Card` du haut, en 1:1). Le contenu du slot est choisi par **priorité** :

```
1. Vidéo READY                          → le LECTEUR (remplace l'image)
2. Vidéo en traitement + AUCUNE image   → placeholder de chargement (messages du polling)
3. Sinon                                → l'IMAGE
     (= pas de vidéo ; OU vidéo en traitement/échec MAIS il y a une image)
```

Concrètement, à la place du bloc `Image` actuel dans le `Card` du haut :

```kotlin
val video = recipe?.video
val slotModifier = Modifier.fillMaxWidth().aspectRatio(1f)   // même gabarit que l'image
when {
    // 1. Vidéo prête → le lecteur remplace l'image
    video?.status == VideoStatus.READY && video.manifestUrl != null ->
        HlsVideoPlayer(video.manifestUrl, slotModifier)   // resizeMode FIT (letterbox) dans le 1:1

    // 2. Vidéo en traitement, SANS image → placeholder de chargement dans le slot
    video != null &&
        (video.status == VideoStatus.PENDING || video.status == VideoStatus.PROCESSING) &&
        recipe.imageUrl.isNullOrBlank() ->
        ProcessingPlaceholder(slotModifier)   // spinner + "Vidéo en cours de traitement…"

    // 3. Sinon → l'image (pas de vidéo ; ou vidéo en traitement/échec avec image)
    else ->
        Image(
            painter = rememberAsyncImagePainter(imageRequestBuilder(context, recipe?.imageUrl ?: "")),
            modifier = slotModifier, contentScale = ContentScale.Crop, contentDescription = null,
        )
}
```

Conséquence de l'« eventual consistency » : quand une recette a **image + vidéo**, on voit
l'image, puis le **polling** fait passer `video.status` à `READY`, et le slot **bascule
automatiquement** sur le lecteur (recomposition).

**Optionnel** : dans le cas 3 avec une vidéo en `PENDING`/`PROCESSING` (donc image affichée
mais vidéo en route), superposer un petit badge « vidéo en traitement… » sur l'image pour
signaler qu'une vidéo va apparaître. Non requis en Phase A.

Note : le slot reste en **1:1** (comme l'image) ; le lecteur affiche la vidéo en `FIT`
(letterbox) pour la montrer entière dans ce carré. On pourra passer le slot en 16:9 quand une
vidéo est présente si tu préfères — à décider à l'implémentation.

### c) Polling dans `CustomRecipeDetailViewModel`

```kotlin
private fun startVideoPollingIfNeeded() {
    viewModelScope.launch {
        while (isActive) {
            val status = state.value.recipe?.video?.status
            if (status == null || status == VideoStatus.READY || status == VideoStatus.FAILED) break
            delay(4_000)
            val refreshed = favoritesRepository.getCustomRecipe(recipeId.orEmpty())
            _state.update { it.copy(recipe = refreshed.getOrNull()) }
        }
    }
}
```

Propriétés : **arrêt automatique** dès `READY`/`FAILED` (ou pas de vidéo) ; **lié au cycle de
vie** (annulé quand l'écran est quitté via `viewModelScope`). Appelé après le chargement
initial de la recette.

## 9. Dépendances & lifecycle

Ajouter au catalogue (`libs.versions.toml`) et à `core/designsystem` :

```
androidx.media3:media3-exoplayer
androidx.media3:media3-exoplayer-hls      # support HLS (.m3u8)
androidx.media3:media3-ui                 # PlayerView
```

Lifecycle : géré dans `HlsVideoPlayer` (pause sur `ON_STOP`, `release()` sur `onDispose`).

## 10. Erreurs & garde-fous

- **Upload** : le backend renvoie `413` (trop lourd) / `400` (pas une vidéo). Snackbar
  générique (comme le flux actuel) ; différenciation du 413 (« Vidéo trop volumineuse »)
  optionnelle.
- **Garde-fou client** : avant upload, vérifier la taille de la vidéo via `ContentResolver`
  et refuser au-delà de ~90 Mo (sous le plafond Cloudflare de 100 Mo) avec un message.
- **Lecture** : le lecteur n'est affiché que si `status == READY`. Un
  `Player.Listener.onPlayerError` affiche un message + « réessayer ».

## 11. Tests

- **Unitaires** : mapper `NetworkVideo → CustomVideo` (dont statut inconnu → `FAILED`) ;
  logique de polling du ViewModel (faux repo renvoyant `PENDING` puis `READY` → vérifier la
  mise à jour de l'état et l'**arrêt** du polling).
- **Manuel/instrumenté** : `HlsVideoPlayer`, sélection galerie.

## 12. Fichiers touchés

| Module | Fichier | Action |
|---|---|---|
| core/network | `model/NetworkCustomRecipe.kt` | `NetworkVideo` + champ `video` + mappers |
| core/network | `api/FavoriteApi.kt` | partie `video` dans `addRecipe` |
| core/model | `CustomRecipe.kt` | `CustomVideo` + enum `VideoStatus` + champ `video` |
| core/data | `favorite/FavoriteManager.kt` | `video` dans `createRecipe` |
| core/data | `repository/FavoritesRepositoryImpl.kt` + interface | `video` dans `addCustomRecipe`, `getCustomRecipe` |
| feature/add_recipe | `AddRecipeViewModel.kt` | `videoUri` (state/action), submit |
| feature/add_recipe | `AddRecipeScreen.kt` | bouton Gallery vidéo + indicateur |
| core/designsystem | `component/HlsVideoPlayer.kt` (nouveau) | lecteur Media3 |
| feature/favorites | `CustomRecipeDetailViewModel.kt` | polling + `getCustomRecipe` |
| feature/favorites | `CustomRecipeDetailScreen.kt` | section vidéo pilotée par statut |
| build | `libs.versions.toml` + `core/designsystem/build.gradle.kts` | dépendances Media3 |

Chaque couche reproduit le chemin existant de l'image ; la seule vraie nouveauté est le
lecteur HLS (Media3) et la boucle de polling.
