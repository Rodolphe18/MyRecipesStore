package com.francotte.myrecipesstore.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.francotte.myrecipesstore.ui.compose.video.findActivity

@Composable
fun NotificationPermissionEffect() {
    if (LocalInspectionMode.current) return
   // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Toast.makeText(
                context,
                if (isGranted) "Permission granted ✅" else "Permission denied ❌",
                Toast.LENGTH_SHORT
            ).show()
        }
    )

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context.findActivity(),
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (!hasPermission && !shouldShowRationale) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}