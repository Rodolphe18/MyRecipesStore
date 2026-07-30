package com.francotte.record_video

import android.annotation.SuppressLint
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.io.File

/**
 * Écran de capture vidéo maison (CameraX).
 * @param onRecorded appelé avec l'Uri du fichier .mp4 une fois l'enregistrement terminé sans erreur.
 * @param onCancel   appelé si l'utilisateur annule (bouton "Annuler").
 */
@SuppressLint("MissingPermission")
@Composable
fun CameraContent(onRecorded: (Uri) -> Unit, onCancel: () -> Unit) {
    // Contexte Android courant (nécessaire pour créer la PreviewView, accéder au cache, etc.).
    val context = LocalContext.current
    // Propriétaire du cycle de vie : CameraX s'y "attache" pour démarrer/arrêter la caméra automatiquement.
    val lifecycleOwner = LocalLifecycleOwner.current
    // La vue Android (View classique) qui affiche le flux de la caméra. `remember` = créée une seule fois.
    val previewView = remember { PreviewView(context) }

    // Objet CameraX qui sait enregistrer une vidéo. null tant que la caméra n'est pas liée.
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    // Poignée sur la caméra active : sert au zoom, à la mise au point, au torch (flash).
    var camera by remember { mutableStateOf<Camera?>(null) }
    // L'enregistrement EN COURS (start/pause/resume/stop). null quand on n'enregistre pas.
    var recording by remember { mutableStateOf<Recording?>(null) }
    // true pendant qu'on filme : pilote l'affichage (bouton Stop, minuteur, masquage du flip).
    var isRecording by remember { mutableStateOf(false) }
    // true si l'enregistrement est en pause (le minuteur ne compte plus).
    var isPaused by remember { mutableStateOf(false) }
    // Secondes écoulées d'enregistrement (pour l'affichage mm:ss et l'auto-stop à 240 s).
    var elapsed by remember { mutableIntStateOf(0) }
    // Caméra choisie : arrière par défaut ; peut basculer vers l'avant.
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    // État du flash/torche (allumé/éteint).
    var torchOn by remember { mutableStateOf(false) }

    // Effet relancé à CHAQUE changement de `cameraSelector` (donc au démarrage et à chaque flip caméra).
    LaunchedEffect(cameraSelector) {
        // Récupère le fournisseur de caméra CameraX de façon asynchrone (await = suspend, pas de callback).
        val provider = ProcessCameraProvider.getInstance(context).await()
        // Cas d'usage "aperçu" : branche le flux caméra sur notre PreviewView.
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        // Enregistreur configuré en qualité SD (~480p) → fichiers légers, conforme au plafond qu'on s'est fixé.
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        // Cas d'usage "capture vidéo" basé sur cet enregistreur.
        val capture = VideoCapture.withOutput(recorder)
        // On délie tout usage précédent avant de relier (obligatoire quand on change de caméra).
        provider.unbindAll()
        // Lie aperçu + capture au cycle de vie et à la caméra sélectionnée ; renvoie la poignée `camera`.
        camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
        // Mémorise l'objet de capture pour pouvoir lancer/arrêter l'enregistrement plus bas.
        videoCapture = capture
    }

    // Minuteur + auto-stop à 240 s (ne compte pas pendant la pause).
    // Relancé quand isRecording ou isPaused change ; la boucle ne tourne que si on filme ET qu'on n'est pas en pause.
    LaunchedEffect(isRecording, isPaused) {
        while (isRecording && !isPaused) {
            delay(1_000)              // attend 1 seconde
            elapsed += 1              // incrémente le compteur
            if (elapsed >= 240) recording?.stop()  // limite dure : coupe automatiquement à 4 min
        }
    }

    // Conteneur plein écran qui empile l'aperçu (fond) et les contrôles (par-dessus).
    Box(
        Modifier
            .fillMaxSize()
            // Gestes multi-doigts (pincement) = zoom.
            .pointerInput(camera) {
                detectTransformGestures { _, _, zoom, _ ->
                    // Zoom courant de la caméra (1f si indisponible).
                    val current = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                    // Applique le nouveau ratio = ratio courant × facteur du pincement.
                    camera?.cameraControl?.setZoomRatio(current * zoom)
                }
            }
            // Appui simple = mise au point à l'endroit touché.
            .pointerInput(camera) {
                detectTapGestures { offset ->
                    // Convertit les coordonnées de l'écran en "point de mesure" pour la caméra.
                    val point = previewView.meteringPointFactory.createPoint(offset.x, offset.y)
                    // Déclenche autofocus + mesure d'exposition sur ce point.
                    camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
                }
            },
    ) {
        // Insère la PreviewView (View Android) dans Compose, en plein écran.
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Minuteur mm:ss affiché en haut au centre, uniquement pendant l'enregistrement.
        if (isRecording) {
            Text(
                "%02d:%02d".format(elapsed / 60, elapsed % 60),  // formate 65 s → "01:05"
                color = Color.White,
                // statusBarsPadding() = descend sous la barre d'état pour ne pas être masqué.
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(16.dp),
            )
        }

        // Bouton "Annuler" en haut à gauche : quitte l'écran sans rien enregistrer.
        TextButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp),
        ) {
            Text("Annuler", color = Color.White)
        }

        // Barre de contrôles en haut à droite : torche + bascule caméra.
        Row(
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            // Bouton torche : inverse l'état et l'applique à la caméra.
            IconButton(onClick = {
                torchOn = !torchOn
                camera?.cameraControl?.enableTorch(torchOn)
            }) {
                // Icône pleine si allumé, barrée si éteint.
                Icon(if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff, null, tint = Color.White)
            }
            // La bascule caméra re-binde CameraX (donc coupe l'enregistrement) → seulement hors enregistrement.
            if (!isRecording) {
                IconButton(onClick = {
                    // Alterne avant/arrière → déclenche le LaunchedEffect(cameraSelector) qui re-lie la caméra.
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                }) {
                    Icon(Icons.Default.Cameraswitch, null, tint = Color.White)
                }
            }
        }

        // Barre d'actions principale en bas au centre : pause/reprise + REC/Stop.
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Bouton pause/reprise, visible seulement pendant l'enregistrement.
            if (isRecording) {
                IconButton(onClick = {
                    if (isPaused) {
                        recording?.resume()
                        isPaused = false  // reprend l'enregistrement
                    } else {
                        recording?.pause()
                        isPaused = true     // met en pause (le minuteur se fige)
                    }
                }) {
                    // Icône "lecture" si en pause (pour reprendre), "pause" sinon.
                    Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = Color.White)
                }
            }
            // Bouton principal : démarre l'enregistrement, ou l'arrête s'il est déjà en cours.
            Button(onClick = {
                if (isRecording) {
                    recording?.stop()  // arrêt manuel → déclenchera l'événement Finalize plus bas
                } else {
                    // Fichier de sortie dans le cache de l'app, nom unique via l'horodatage.
                    val file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.mp4")
                    // Prépare et démarre l'enregistrement vers ce fichier.
                    recording = videoCapture?.output
                        ?.prepareRecording(context, FileOutputOptions.Builder(file).build())
                        ?.withAudioEnabled()  // active la piste audio (micro)
                        // Callback appelé sur le thread principal à chaque événement d'enregistrement.
                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                // Enregistrement effectivement démarré → on met à jour l'UI et on remet le compteur à 0.
                                is VideoRecordEvent.Start -> {
                                    isRecording = true
                                    isPaused = false
                                    elapsed = 0
                                }
                                // Enregistrement terminé (stop manuel, auto-stop, ou erreur).
                                is VideoRecordEvent.Finalize -> {
                                    isRecording = false
                                    isPaused = false
                                    // Pas d'erreur → on remonte l'Uri du fichier ; sinon on supprime le fichier corrompu.
                                    if (!event.hasError()) onRecorded(file.toUri()) else file.delete()
                                }
                                // Autres événements (Status, Pause, Resume…) : ignorés ici.
                                else -> Unit
                            }
                        }
                }
            }) {
                Text(if (isRecording) "Stop" else "REC")
            }
        }
    }
}
