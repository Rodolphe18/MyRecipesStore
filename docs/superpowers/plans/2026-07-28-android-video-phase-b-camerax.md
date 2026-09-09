# Enregistrement vidéo CameraX (Phase B) — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. (Ce projet exécute toujours en inline.)

**Goal:** Enregistrer une vidéo in-app via CameraX (480p, 240 s max, micro), avec contrôles riches (pause/reprise, flash, avant/arrière, zoom, tap-focus) et une étape de relecture, puis renvoyer l'Uri à l'écran de création de recette.

**Architecture:** Nouveau module `feature/record_video` (api + impl) avec un écran CameraX navigable (`RecordVideoNavKey`). L'Uri enregistrée revient à `add_recipe` via un bus de résultat `@Singleton` (`VideoRecorderResultBus` dans `core/common`), collecté par `AddRecipeViewModel`.

**Tech Stack:** Kotlin, Compose, Hilt, Navigation3, CameraX (camera-video), Media3 ExoPlayer (relecture, déjà présent).

**Spec de référence :** `docs/superpowers/specs/2026-07-28-android-video-phase-b-camerax-design.md`

**Build (Windows/PowerShell) :** `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; .\gradlew.bat <tâche>`

---

## Décisions verrouillées

Écran dédié (NavKey) ; module `feature/record_video` ; 480p (`Quality.SD`) ; 240 s auto-stop + arrêt manuel ; micro ; retour via bus ; toute la Section 5 UX (pause/reprise, flash, avant/arrière, zoom, tap-focus, relecture).

## Structure des fichiers

| Module | Fichier | Action |
|---|---|---|
| gradle | `gradle/libs.versions.toml` | alias CameraX + concurrent-futures |
| core/common | `.../VideoRecorderResultBus.kt` (nouveau) + `PermissionExt.kt` | bus + helper permissions |
| feature/record_video/api | `build.gradle.kts`, `RecordVideoNavKey.kt` | nouveau module |
| feature/record_video/impl | `build.gradle.kts`, `AndroidManifest.xml`, `RecordVideoNavKey`→ entry, `RecordVideoScreen.kt`, `RecordVideoViewModel.kt`, `RecordVideoRoute.kt` | nouveau module |
| settings.gradle.kts | includes | +2 modules |
| app | `build.gradle.kts` (+2 deps), `ui/FoodApp.kt` (entry) | câblage nav |
| feature/add_recipe/impl | `AddRecipeViewModel.kt`, `AddRecipeScreen.kt`, `AddRecipeRoute.kt`, `build.gradle.kts` | bouton Record + collecte bus |

---

## Task 0 : Dépendances CameraX

**Files:** `gradle/libs.versions.toml`

- [ ] **Step 1 : Versions + alias**

Sous `[versions]` :
```toml
camerax = "1.4.1"
concurrentFutures = "1.2.0"
```
Sous `[libraries]` :
```toml
androidx-camera-core = { module = "androidx.camera:camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { module = "androidx.camera:camera-view", version.ref = "camerax" }
androidx-camera-video = { module = "androidx.camera:camera-video", version.ref = "camerax" }
androidx-concurrent-futures-ktx = { module = "androidx.concurrent:concurrent-futures-ktx", version.ref = "concurrentFutures" }
```

- [ ] **Step 2 : Vérifier le catalogue**

Run: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; cd "C:\Users\rodol\AndroidStudioProjects\MyRecipesStore"; .\gradlew.bat help -q`
Expected: `BUILD SUCCESSFUL` (le TOML se parse).

- [ ] **Step 3 : Commit** — `git add gradle/libs.versions.toml && git commit -m "chore: add CameraX dependencies"`

---

## Task 1 : Le bus de résultat (`core/common`) — TDD

**Files:**
- Create: `core/common/src/main/java/com/francotte/common/VideoRecorderResultBus.kt`
- Create: `core/common/src/test/java/com/francotte/common/VideoRecorderResultBusTest.kt`

- [ ] **Step 1 : Écrire le test**

Créer `VideoRecorderResultBusTest.kt` :
```kotlin
package com.francotte.common

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoRecorderResultBusTest {
    @Test
    fun `emitted uri is received by a collector`() = runTest {
        val bus = VideoRecorderResultBus()
        val uri = mockk<Uri>()
        var received: Uri? = null
        val job = launch { bus.results.collect { received = it } }
        runCurrent()
        bus.emit(uri)
        runCurrent()
        assertEquals(uri, received)
        job.cancel()
    }
}
```

- [ ] **Step 2 : Lancer → échec (classe absente)**

Run: `.\gradlew.bat :core:common:testDebugUnitTest --tests "com.francotte.common.VideoRecorderResultBusTest"`
Expected: FAIL (compilation).

- [ ] **Step 3 : Implémenter le bus**

Créer `VideoRecorderResultBus.kt` :
```kotlin
package com.francotte.common

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRecorderResultBus @Inject constructor() {
    private val _results = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val results: SharedFlow<Uri> = _results.asSharedFlow()
    suspend fun emit(uri: Uri) { _results.emit(uri) }
}
```

> Si `core/common` n'a pas `kotlinx-coroutines-test`/`mockk` en test, ajouter au `build.gradle.kts` : `testImplementation(libs.kotlinx.coroutines.test)` et `testImplementation(libs.mockk)`. Vérifier d'abord les alias existants (`mockk` est au catalogue).

- [ ] **Step 4 : Lancer → succès** — `.\gradlew.bat :core:common:testDebugUnitTest --tests "com.francotte.common.VideoRecorderResultBusTest"` → PASS.

- [ ] **Step 5 : Helper permissions**

Créer/compléter `core/common/src/main/java/com/francotte/common/PermissionExt.kt` :
```kotlin
package com.francotte.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.hasCameraAndAudio(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
```

- [ ] **Step 6 : Commit** — `git add core/common && git commit -m "feat(common): video recorder result bus + permission helper"`

---

## Task 2 : Module `feature/record_video/api`

**Files:**
- Create: `feature/record_video/api/build.gradle.kts`
- Create: `feature/record_video/api/src/main/AndroidManifest.xml`
- Create: `feature/record_video/api/src/main/java/com/francotte/record_video/api/RecordVideoNavKey.kt`
- Modify: `settings.gradle.kts`

- [ ] **Step 1 : `settings.gradle.kts`** — ajouter (près des autres `feature:*:api`/`impl`) :
```kotlin
include(":feature:record_video:api")
include(":feature:record_video:impl")
```

- [ ] **Step 2 : `build.gradle.kts` (api)** — copie de `feature/add_recipe/api/build.gradle.kts` en changeant le namespace :
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}
android {
    namespace = "com.francotte.record_video.api"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    api(project(":core:navigation"))
    implementation(libs.androidx.core.ktx)
}
```

- [ ] **Step 3 : `AndroidManifest.xml` (api)** — minimal :
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

- [ ] **Step 4 : `RecordVideoNavKey.kt`** :
```kotlin
package com.francotte.record_video.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object RecordVideoNavKey : NavKey

fun Navigator.navigateToRecordVideo() { navigate(RecordVideoNavKey) }
```

- [ ] **Step 5 : Compiler** — `.\gradlew.bat :feature:record_video:api:compileDebugKotlin` → `BUILD SUCCESSFUL`.

- [ ] **Step 6 : Commit** — `git add settings.gradle.kts feature/record_video/api && git commit -m "feat(record_video): api module with nav key"`

---

## Task 3 : Module `feature/record_video/impl` (squelette qui compile)

**Files:**
- Create: `feature/record_video/impl/build.gradle.kts`
- Create: `feature/record_video/impl/src/main/AndroidManifest.xml`
- Create: `.../impl/.../RecordVideoViewModel.kt`, `RecordVideoScreen.kt`, `RecordVideoRoute.kt`
- Modify: `app/build.gradle.kts`, `app/.../ui/FoodApp.kt`

- [ ] **Step 1 : `build.gradle.kts` (impl)** — sur le modèle de `feature/add_recipe/impl`, avec CameraX + Compose + Hilt :
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.francotte.record_video"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    api(project(":core:common"))
    api(project(":core:designsystem"))
    api(project(":core:navigation"))
    api(project(":feature:record_video:api"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.iconsExtended)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.concurrent.futures.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}
```

- [ ] **Step 2 : `AndroidManifest.xml` (impl)** — permissions :
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-feature android:name="android.hardware.camera.any" android:required="false" />
</manifest>
```

- [ ] **Step 3 : Squelette Screen/VM/Route (compile, sans caméra encore)**

`RecordVideoViewModel.kt` :
```kotlin
package com.francotte.record_video

import androidx.lifecycle.ViewModel
import com.francotte.common.VideoRecorderResultBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordVideoViewModel @Inject constructor(
    val resultBus: VideoRecorderResultBus,
) : ViewModel()
```

`RecordVideoScreen.kt` :
```kotlin
package com.francotte.record_video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RecordVideoScreen(onDone: () -> Unit) {
    Box(Modifier.fillMaxSize()) { Text("Record (à implémenter)") }
}
```

`RecordVideoRoute.kt` :
```kotlin
package com.francotte.record_video

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import com.francotte.record_video.api.RecordVideoNavKey

fun EntryProviderScope<NavKey>.recordVideoEntry(navigator: Navigator) {
    entry<RecordVideoNavKey> {
        RecordVideoRoute(onDone = { navigator.goBack() })
    }
}

@Composable
fun RecordVideoRoute(onDone: () -> Unit, viewModel: RecordVideoViewModel = hiltViewModel()) {
    RecordVideoScreen(onDone = onDone)
}
```

- [ ] **Step 4 : Câbler l'app** — `app/build.gradle.kts`, ajouter :
```kotlin
    implementation(project(":feature:record_video:api"))
    implementation(project(":feature:record_video:impl"))
```
`app/.../ui/FoodApp.kt`, importer `import com.francotte.record_video.recordVideoEntry` et l'ajouter dans le bloc `entryProvider { … }` (après `addRecipeEntry(navigator)`) :
```kotlin
        recordVideoEntry(navigator)
```

- [ ] **Step 5 : Compiler l'app** — `.\gradlew.bat :app:assembleDebug` → `BUILD SUCCESSFUL`.

- [ ] **Step 6 : Commit** — `git add settings.gradle.kts feature/record_video app && git commit -m "feat(record_video): impl module skeleton wired into nav"`

---

## Task 4 : Permissions dans l'écran

**Files:** `feature/record_video/impl/.../RecordVideoScreen.kt`

- [ ] **Step 1 : Gérer l'état des permissions**

Remplacer le corps de `RecordVideoScreen` pour demander CAMERA+RECORD_AUDIO à l'ouverture et ne montrer la caméra que si accordées :
```kotlin
@Composable
fun RecordVideoScreen(onRecorded: (Uri) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(context.hasCameraAndAudio()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> hasPermissions = result.values.all { it } }
    LaunchedEffect(Unit) {
        if (!hasPermissions) launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }
    if (!hasPermissions) {
        PermissionRequest(
            onRequest = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
            },
            onCancel = onCancel,
        )
        return
    }
    CameraContent(onRecorded = onRecorded, onCancel = onCancel)   // Task 5
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit, onOpenSettings: () -> Unit, onCancel: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Autorise la caméra et le micro pour filmer ta recette.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequest) { Text("Autoriser") }
        TextButton(onClick = onOpenSettings) { Text("Ouvrir les paramètres") }
        TextButton(onClick = onCancel) { Text("Retour") }
    }
}
```
Imports : `android.Manifest`, `android.content.Intent`, `android.net.Uri`, `android.provider.Settings`, `androidx.activity.compose.rememberLauncherForActivityResult`, `androidx.activity.result.contract.ActivityResultContracts`, `com.francotte.common.hasCameraAndAudio`, layout/material3/runtime usuels.

- [ ] **Step 2 : Rendre le Route conforme à la nouvelle signature** — dans `RecordVideoRoute`, passer `onRecorded`/`onCancel` (le `onRecorded` sera relié au bus en Task 7 ; pour l'instant `onRecorded = { onDone() }`, `onCancel = onDone`).

- [ ] **Step 3 : Compiler** — `.\gradlew.bat :feature:record_video:impl:compileDebugKotlin` → `BUILD SUCCESSFUL`.

- [ ] **Step 4 : Commit** — `git add feature/record_video && git commit -m "feat(record_video): runtime permission flow"`

---

## Task 5 : CameraX — aperçu, enregistrement, 480p, 240 s

**Files:** `feature/record_video/impl/.../CameraContent.kt` (nouveau)

- [ ] **Step 1 : Le composable caméra**

Créer `CameraContent.kt` (aperçu + REC/stop + 480p + audio + auto-stop 240 s + minuteur) :
```kotlin
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun CameraContent(onRecorded: (Uri) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var elapsed by remember { mutableIntStateOf(0) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(cameraSelector) {
        val provider = ProcessCameraProvider.getInstance(context).await()
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.SD)).build()
        val capture = VideoCapture.withOutput(recorder)
        provider.unbindAll()
        camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
        videoCapture = capture
    }

    // Minuteur + auto-stop à 240 s
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1_000); elapsed += 1
            if (elapsed >= 240) recording?.stop()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        // minuteur
        if (isRecording) Text("%02d:%02d".format(elapsed / 60, elapsed % 60), color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
        // bouton REC / stop
        Button(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            onClick = {
                if (isRecording) { recording?.stop() } else {
                    val out = File(context.cacheDir, "rec_${System.currentTimeMillis()}.mp4")
                    recording = videoCapture?.output
                        ?.prepareRecording(context, FileOutputOptions.Builder(out).build())
                        ?.withAudioEnabled()
                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> { isRecording = true; elapsed = 0 }
                                is VideoRecordEvent.Finalize -> {
                                    isRecording = false
                                    if (!event.hasError()) onRecorded(out.toUri())
                                    else out.delete()
                                }
                                else -> Unit
                            }
                        }
                }
            },
        ) { Text(if (isRecording) "Stop" else "REC") }
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) { Text("Annuler", color = Color.White) }
    }
}
```
Imports CameraX : `androidx.camera.core.*` (Camera, CameraSelector, Preview), `androidx.camera.lifecycle.ProcessCameraProvider`, `androidx.camera.video.*` (Recorder, Quality, QualitySelector, VideoCapture, Recording, FileOutputOptions, VideoRecordEvent), `androidx.camera.view.PreviewView`, `androidx.concurrent.futures.await`, `androidx.core.content.ContextCompat`, `androidx.core.net.toUri`, `java.io.File`.

- [ ] **Step 2 : Compiler** — `.\gradlew.bat :feature:record_video:impl:compileDebugKotlin` → `BUILD SUCCESSFUL`.

- [ ] **Step 3 : Commit** — `git add feature/record_video && git commit -m "feat(record_video): CameraX preview, 480p recording, 240s auto-stop"`

---

## Task 6 : Contrôles UX (pause/reprise, flash, avant/arrière, zoom, tap-focus)

**Files:** `feature/record_video/impl/.../CameraContent.kt`

- [ ] **Step 1 : Ajouter les contrôles**

Dans `CameraContent`, ajouter les boutons/gestes :
```kotlin
// Pause / reprise (visible seulement pendant l'enregistrement)
var isPaused by remember { mutableStateOf(false) }
// bouton :
if (isRecording) IconButton(onClick = {
    if (isPaused) { recording?.resume(); isPaused = false } else { recording?.pause(); isPaused = true }
}) { Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = Color.White) }

// Flash / torche
var torchOn by remember { mutableStateOf(false) }
IconButton(onClick = { torchOn = !torchOn; camera?.cameraControl?.enableTorch(torchOn) }) {
    Icon(if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff, null, tint = Color.White)
}

// Bascule avant/arrière (rebind via le LaunchedEffect(cameraSelector))
IconButton(onClick = {
    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
        CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
}) { Icon(Icons.Default.Cameraswitch, null, tint = Color.White) }
```
Zoom (pincement) et tap-focus sur le `AndroidView` du preview :
```kotlin
modifier = Modifier.fillMaxSize()
    .pointerInput(camera) {
        detectTransformGestures { _, _, zoom, _ ->
            val current = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
            camera?.cameraControl?.setZoomRatio(current * zoom)
        }
    }
    .pointerInput(camera) {
        detectTapGestures { offset ->
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(offset.x, offset.y)
            camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
        }
    }
```
Imports : `androidx.compose.foundation.gestures.detectTransformGestures`, `detectTapGestures`, `androidx.compose.ui.input.pointer.pointerInput`, `androidx.camera.core.FocusMeteringAction`, icônes material.

- [ ] **Step 2 : Compiler** — `.\gradlew.bat :feature:record_video:impl:compileDebugKotlin` → `BUILD SUCCESSFUL`.

- [ ] **Step 3 : Commit** — `git add feature/record_video && git commit -m "feat(record_video): pause/resume, torch, flip, zoom, tap-focus"`

---

## Task 7 : Étape de relecture + émission vers le bus

**Files:** `feature/record_video/impl/.../RecordVideoScreen.kt`, `RecordVideoRoute.kt`, `RecordVideoViewModel.kt`

- [ ] **Step 1 : État « enregistré → relecture » dans l'écran**

Dans `RecordVideoScreen` (après permissions), gérer deux modes : caméra, puis relecture du fichier local :
```kotlin
var recordedUri by remember { mutableStateOf<Uri?>(null) }
val uri = recordedUri
if (uri == null) {
    CameraContent(onRecorded = { recordedUri = it }, onCancel = onCancel)
} else {
    ReviewContent(
        uri = uri,
        onRetake = { runCatching { context.contentResolver.delete(uri, null, null) }; recordedUri = null },
        onUse = { onUse(uri) },
    )
}
```
`ReviewContent` réutilise un lecteur local (Media3) avec seek bar :
```kotlin
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun ReviewContent(uri: Uri, onRetake: () -> Unit, onUse: () -> Unit) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build().apply { setMediaItem(MediaItem.fromUri(uri)); prepare() } }
    DisposableEffect(Unit) { onDispose { player.release() } }
    Column(Modifier.fillMaxSize()) {
        AndroidView(factory = { PlayerView(it).apply { this.player = player } }, modifier = Modifier.weight(1f).fillMaxWidth())
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(onClick = onRetake) { Text("Refaire") }
            Button(onClick = onUse) { Text("Utiliser cette vidéo") }
        }
    }
}
```
`PlayerView` fournit une **seek bar** + play/pause par défaut. `RecordVideoScreen` reçoit `onUse: (Uri) -> Unit` et `onCancel: () -> Unit`.

- [ ] **Step 2 : Émettre sur le bus (VM) + goBack (Route)**

`RecordVideoViewModel` :
```kotlin
private val _done = Channel<Unit>()
val done = _done.receiveAsFlow()
fun onVideoConfirmed(uri: Uri) = viewModelScope.launch { resultBus.emit(uri); _done.send(Unit) }
```
`RecordVideoRoute` :
```kotlin
@Composable
fun RecordVideoRoute(onDone: () -> Unit, viewModel: RecordVideoViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) { viewModel.done.collect { onDone() } }
    RecordVideoScreen(onUse = viewModel::onVideoConfirmed, onCancel = onDone)
}
```

- [ ] **Step 3 : Compiler** — `.\gradlew.bat :feature:record_video:impl:compileDebugKotlin` → `BUILD SUCCESSFUL`.

- [ ] **Step 4 : Commit** — `git add feature/record_video && git commit -m "feat(record_video): review step + emit uri to result bus"`

---

## Task 8 : Intégration `add_recipe` — TDD sur la collecte

**Files:**
- Modify: `feature/add_recipe/impl/build.gradle.kts` (dep vers `:feature:record_video:api` + `kotlinx-coroutines-test`)
- Modify: `AddRecipeViewModel.kt`, `AddRecipeScreen.kt`, `AddRecipeRoute.kt`
- Create: `feature/add_recipe/impl/src/test/java/com/francotte/add_recipe/AddRecipeBusCollectionTest.kt`

- [ ] **Step 1 : Dépendances** — dans `feature/add_recipe/impl/build.gradle.kts` : `implementation(project(":feature:record_video:api"))` et `testImplementation(libs.kotlinx.coroutines.test)`.

- [ ] **Step 2 : Écrire le test de collecte**

Créer `AddRecipeBusCollectionTest.kt` — le VM doit poser `videoUri` quand le bus émet :
```kotlin
package com.francotte.add_recipe

import android.net.Uri
import com.francotte.common.VideoRecorderResultBus
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddRecipeBusCollectionTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `bus emission sets videoUri`() = runTest(dispatcher.scheduler) {
        val bus = VideoRecorderResultBus()
        val repo = mockk<com.francotte.data.interfaces.FavoritesRepository>(relaxed = true)
        val userRepo = mockk<com.francotte.data.interfaces.UserDataRepository>(relaxed = true)
        val vm = AddRecipeViewModel(repo, userRepo, bus)   // adapter aux vrais paramètres du VM
        val uri = mockk<Uri>()
        advanceUntilIdle()
        bus.emit(uri)
        advanceUntilIdle()
        assertEquals(uri, vm.state.value.videoUri)
    }
}
```
> Adapter la liste des paramètres du constructeur `AddRecipeViewModel` à sa signature réelle (ajouter les `mockk(relaxed = true)` manquants). Le point testé : l'ajout de `videoRecorderResultBus` et sa collecte.

- [ ] **Step 3 : Lancer → échec** — `.\gradlew.bat :feature:add_recipe:impl:testDebugUnitTest --tests "com.francotte.add_recipe.AddRecipeBusCollectionTest"` → FAIL.

- [ ] **Step 4 : Injecter + collecter le bus dans `AddRecipeViewModel`**

Ajouter `private val videoRecorderResultBus: VideoRecorderResultBus` au constructeur, et dans `init` :
```kotlin
        videoRecorderResultBus.results
            .onEach { uri -> _state.update { it.copy(videoUri = uri) } }
            .launchIn(viewModelScope)
```
(imports `com.francotte.common.VideoRecorderResultBus`, `onEach`, `launchIn`.)

- [ ] **Step 5 : Lancer → succès** — même commande → PASS.

- [ ] **Step 6 : Bouton « Record » + navigation**

`AddRecipeScreen.kt` : sous le bouton « Gallery » vidéo (Phase A), ajouter :
```kotlin
                    Spacer(Modifier.height(8.dp))
                    Button(modifier = Modifier.height(40.dp), onClick = { onAction(AddRecipeAction.OnRecordVideo) }) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(Modifier.width(6.dp)); Text("Record", fontSize = 12.sp)
                    }
```
`AddRecipeViewModel.kt` : ajouter `data object OnRecordVideo : AddRecipeAction` (à côté de `OnGoToLogin`).
`AddRecipeRoute.kt` : ajouter le paramètre de navigation et le routage :
```kotlin
fun EntryProviderScope<NavKey>.addRecipeEntry(navigator: Navigator) {
    entry<AddRecipeNavKey> {
        AddRoute(
            goToLoginScreen = navigator::navigateToLogin,
            goToRecordVideo = { navigator.navigate(com.francotte.record_video.api.RecordVideoNavKey) },
        )
    }
}
// AddRoute :
fun AddRoute(goToLoginScreen: () -> Unit, goToRecordVideo: () -> Unit, viewModel: AddRecipeViewModel = hiltViewModel()) { ... }
// dans onAction :
    AddRecipeAction.OnRecordVideo -> goToRecordVideo()
```

- [ ] **Step 7 : Compiler l'app** — `.\gradlew.bat :app:assembleDebug` → `BUILD SUCCESSFUL`.

- [ ] **Step 8 : Commit** — `git add feature/add_recipe && git commit -m "feat(add_recipe): record button + collect recorded video from bus"`

---

## Task 9 : Build complet + vérification manuelle

- [ ] **Step 1 : Assembler** — `.\gradlew.bat :app:assembleDebug` → `BUILD SUCCESSFUL`.

- [ ] **Step 2 : Vérif manuelle (appareil réel avec caméra)**
1. Add recipe → bouton **Record** → écran d'enregistrement.
2. Accorder caméra + micro. Filmer : REC, **pause/reprise**, **flash**, **avant/arrière**, **zoom** (pincement), **tap-focus**. Vérifier l'auto-stop à 240 s.
3. **Relecture** : play/pause + **seek bar**. « Refaire » relance la caméra ; « Utiliser » revient à add_recipe avec « vidéo sélectionnée ✓ ».
4. Soumettre la recette → upload (Phase A) → lecture HLS dans le détail.

---

## Auto-revue (couverture spec ↔ plan)

- **Spec §3 (architecture, bus)** → Tasks 1, 7, 8.
- **Spec §4 (CameraX 480p/240s)** → Task 5.
- **Spec §5 (UX : pause/flash/flip/zoom/focus + relecture)** → Tasks 6, 7.
- **Spec §6 (permissions)** → Task 4.
- **Spec §7 (intégration add_recipe)** → Task 8.
- **Spec §8 (scaffolding module)** → Tasks 2, 3.
- **Spec §9 (dépendances)** → Task 0.
- **Spec §10 (erreurs)** → Tasks 4 (permissions), 5 (Finalize/erreur + suppression fichier), 7 (retake supprime).
- **Spec §11 (tests)** → Tasks 1 (bus), 8 (collecte add_recipe).

Gaps assumés : garde-fou de taille à l'upload (§10) non implémenté (peu probable en 480p ; à ajouter si souhaité) ; tests CameraX = manuels (matériel).
