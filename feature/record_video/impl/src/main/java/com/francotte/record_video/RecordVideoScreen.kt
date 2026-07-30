package com.francotte.record_video

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.francotte.common.hasCameraAndAudio
import com.francotte.designsystem.component.HlsVideoPlayer

@Composable
fun RecordVideoScreen(onUse: (Uri) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(context.hasCameraAndAudio()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> hasPermissions = result.values.all { it } }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    if (!hasPermissions) {
        PermissionRequest(
            onRequest = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
            onOpenSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
            },
            onCancel = onCancel,
        )
        return
    }

    var recordedUri by remember { mutableStateOf<Uri?>(null) }
    val uri = recordedUri
    if (uri == null) {
        CameraContent(onRecorded = { recordedUri = it }, onCancel = onCancel)
    } else {
        ReviewContent(
            uri = uri,
            onRetake = {
                runCatching { context.contentResolver.delete(uri, null, null) }
                recordedUri = null
            },
            onUse = { onUse(uri) },
        )
    }
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit, onOpenSettings: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Autorise la caméra et le micro pour filmer ta recette.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequest) { Text("Autoriser") }
        TextButton(onClick = onOpenSettings) { Text("Ouvrir les paramètres") }
        TextButton(onClick = onCancel) { Text("Retour") }
    }
}

@Composable
private fun ReviewContent(uri: Uri, onRetake: () -> Unit, onUse: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        HlsVideoPlayer(uri.toString(), Modifier.weight(1f).fillMaxWidth())
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            OutlinedButton(onClick = onRetake) { Text("Refaire") }
            Button(onClick = onUse) { Text("Utiliser cette vidéo") }
        }
    }
}
