# Design — Intégration vidéo Android, Phase B (enregistrement CameraX)

**Date :** 2026-07-28
**Projet :** MyRecipesStore (app Android)
**Objectif :** ajouter l'enregistrement vidéo **in-app** via CameraX comme 2ᵉ source (à côté
de la galerie de la Phase A), dans un écran dédié, avec une expérience riche (pause/reprise,
flash, zoom, relecture) et un retour de l'Uri enregistrée vers l'écran de création de recette.

**Prérequis :** Phase A livrée (le champ `videoUri` traverse déjà add_recipe → data → backend ;
`OnVideoChange(uri)` existe). Voir `2026-07-28-android-video-phase-a-design.md`.

---

## 1. Périmètre

**Phase B (cette spec)** : enregistrer une vidéo dans l'app, la relire, la confirmer, et la
passer à `add_recipe` (qui l'upload via le flux de la Phase A). La vidéo enregistrée alimente
le même `videoUri` que la galerie.

**Hors périmètre** : montage/trim, choix de qualité par l'utilisateur (480p fixe), HDR,
réglage d'exposition ; édition de la vidéo d'une recette existante (Phase D).

## 2. Décisions verrouillées

1. **Écran dédié** (nouveau `RecordVideoNavKey`), dans un **nouveau module `feature/record_video`** (api + impl).
2. **480p** via `QualitySelector.from(Quality.SD)` (taille maîtrisée pour l'upload < 100 Mo).
3. **240 s auto-stop** + arrêt manuel possible.
4. **Micro activé** (`withAudioEnabled()`).
5. Retour de l'Uri via un **bus de résultat** `@Singleton` (Navigation3 n'a pas de résultat intégré).
6. Le format de sortie (MP4/H.264) est indifférent : le backend re-transcode en HLS.

## 3. Architecture & flux de résultat

```
add_recipe [bouton Record] ──navigate(RecordVideoNavKey)──► RecordVideoScreen
                                                                │ permissions → CameraX → REC
                                                                │ (pause/reprise, flash, zoom…)
                                                                │ stop (manuel ou 240s)
                                                                ▼
                                                            Relecture (play/pause/seek)
                                                                │ "Refaire" ou "Utiliser"
              ◄── goBack() + bus.emit(uri) ◄──────────────────── "Utiliser"
add_recipe (collecte le bus) → videoUri = uri → "vidéo sélectionnée ✓"
```

**Le bus** (dans `core/common`, injectable par Hilt dans les deux features) :
```kotlin
@Singleton
class VideoRecorderResultBus @Inject constructor() {
    private val _results = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val results: SharedFlow<Uri> = _results.asSharedFlow()
    suspend fun emit(uri: Uri) { _results.emit(uri) }
}
```
`add_recipe` restant dans la pile quand on ouvre l'enregistrement, son ViewModel reste vivant
et collecte le bus.

## 4. L'écran d'enregistrement (CameraX)

**Aperçu + capture** :
```kotlin
val previewView = remember { PreviewView(context) }
LaunchedEffect(hasPermissions, cameraFacing) {
    if (!hasPermissions) return@LaunchedEffect
    val provider = ProcessCameraProvider.getInstance(context).await()
    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.SD))   // 480p
        .build()
    videoCapture = VideoCapture.withOutput(recorder)
    provider.unbindAll()
    camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
}
```

**Start / stop** :
```kotlin
val outputFile = File(context.cacheDir, "rec_${System.currentTimeMillis()}.mp4")
recording = videoCapture.output
    .prepareRecording(context, FileOutputOptions.Builder(outputFile).build())
    .withAudioEnabled()
    .start(ContextCompat.getMainExecutor(context)) { event ->
        when (event) {
            is VideoRecordEvent.Finalize ->
                if (!event.hasError()) onRecordingFinished(outputFile.toUri()) else onError()
            else -> Unit
        }
    }
// stop : recording.stop()  (bouton, ou auto à 240 s)
```

**Auto-stop 240 s** : un compteur (coroutine `while` + `delay(1_000)`) incrémente les secondes
et appelle `recording.stop()` à 240 s. Minuteur `mm:ss` visible.

**UI** : aperçu plein écran ; bouton REC rond en bas (rouge, start/stop) entouré d'un **anneau
de progression** vers 240 s ; minuteur en haut ; boutons de contrôle (voir §5) ; bouton
« Annuler » (retour sans émettre).

## 5. Améliorations UX (l'intérêt de CameraX)

Ce que l'écran dédié apporte au-delà d'un simple intent :

- **Pause / reprise** (`Recording.pause()` / `resume()`) — filmer une recette **en plusieurs
  étapes** dans un seul clip. Bouton pause à côté du REC pendant l'enregistrement.
- **Flash / torche** (`camera.cameraControl.enableTorch(true/false)`) — utile en cuisine.
- **Bascule avant/arrière** (`CameraSelector.DEFAULT_FRONT/BACK_CAMERA` + re-bind).
- **Zoom** (pincement → `cameraControl.setZoomRatio`, état via `cameraInfo.zoomState`).
- **Mise au point au toucher** (`cameraControl.startFocusAndMetering(...)`).
- **Étape de relecture** après l'arrêt : lecteur du fichier local (ExoPlayer, réutilisé de la
  Phase A) avec **play/pause + seek bar**, puis deux actions : **« Refaire »** (supprime le
  fichier, retour à la caméra) et **« Utiliser cette vidéo »** (`bus.emit(uri)` + `goBack()`).

> La seek bar n'existe qu'à l'étape **relecture** (on ne seek pas un flux en cours de capture).

Écartés (YAGNI) : trim/montage, choix de qualité, HDR, exposition.

## 6. Permissions

Manifeste (`feature/record_video/impl/AndroidManifest.xml`) :
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

Demande runtime à l'ouverture de l'écran (`RequestMultiplePermissions` pour les deux). États :
- accordées → caméra + REC ;
- premier refus → bouton « Autoriser » (relance la popup) ;
- refus définitif (`shouldShowRequestPermissionRationale == false` après refus) → message +
  bouton « Ouvrir les paramètres » (`ACTION_APPLICATION_DETAILS_SETTINGS`) ;
- « Retour » → `goBack()`.

Tant que non accordées, **ne pas binder** CameraX (le bind dépend de `hasPermissions`). Helper
`Context.hasCameraAndAudio()` (dans `core/common`).

## 7. Intégration `add_recipe`

- **Bouton « Record »** dans `AddRecipeScreen`, à côté de « Gallery » → action `OnRecordVideo`.
- **`AddRoute`** route `OnRecordVideo` vers `navigator.navigate(RecordVideoNavKey)` (comme
  `OnGoToLogin` aujourd'hui) ; `addRecipeEntry` fournit `goToRecordVideo = { navigator.navigate(RecordVideoNavKey) }`.
- **`AddRecipeViewModel`** injecte `VideoRecorderResultBus` et collecte dans `init` :
  ```kotlin
  videoRecorderResultBus.results
      .onEach { uri -> _state.update { it.copy(videoUri = uri) } }
      .launchIn(viewModelScope)
  ```

## 8. Scaffolding du module `feature/record_video`

- `settings.gradle.kts` : inclure `:feature:record_video:api` et `:feature:record_video:impl`.
- `api` : `RecordVideoNavKey` (NavKey `@Serializable`).
- `impl` : `RecordVideoScreen`, `RecordVideoViewModel`, `RecordVideoRoute` (+ `recordVideoEntry`),
  `AndroidManifest.xml` (permissions), `build.gradle.kts` (CameraX + Hilt + Compose).
- **app** : enregistrer `recordVideoEntry(navigator)` dans le graphe de navigation (là où
  `addRecipeEntry` est déclarée).

## 9. Dépendances

Catalogue + `feature/record_video/impl` :
```
androidx.camera:camera-core, camera-camera2, camera-lifecycle, camera-view, camera-video  (1.4.1)
androidx.concurrent:concurrent-futures-ktx   # .await() sur le ListenableFuture de getInstance
androidx.media3:media3-exoplayer(-ui)         # relecture locale (déjà au catalogue via Phase A)
```

## 10. Erreurs

| Cas | Traitement |
|---|---|
| Permission refusée | écran de demande → paramètres si refus définitif |
| Bind caméra échoue | message + `goBack()` |
| `Finalize` en erreur | « échec de l'enregistrement », possibilité de refaire |
| Retour pendant l'enregistrement | `recording.stop()` + supprimer le fichier partiel, ne pas émettre |
| Fichier > ~90 Mo (sécurité) | garde-fou de taille à l'upload (peu probable en 480p) |

## 11. Tests

- **Unitaire** : le **bus de résultat** (émettre une Uri → un collecteur la reçoit) ; la
  **collecte dans `AddRecipeViewModel`** (bus réel + `runTest` → `videoUri` mis à jour).
- **Manuel/instrumenté** : écran CameraX, permissions, pause/reprise, auto-stop 240 s,
  relecture (matériel caméra requis).

## 12. Hors périmètre

Trim/montage, choix de qualité utilisateur, HDR/exposition, édition de la vidéo d'une recette
existante (Phase D), enregistrement en arrière-plan.
