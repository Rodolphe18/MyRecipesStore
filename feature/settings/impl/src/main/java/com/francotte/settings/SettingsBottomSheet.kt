package com.francotte.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.francotte.designsystem.theme.LightOrange
import com.francotte.designsystem.theme.Orange
import com.francotte.designsystem.R as DesignR

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Try this app! it is incredible!")
        putExtra(Intent.EXTRA_TITLE, "My recipes Store")
        putExtra(Intent.EXTRA_SUBJECT, "Food recipes")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}

private fun openPrivacyPolicy(context: Context) {
    openUrlRobust(context, "https://myrecipesstore18.com/privacy-policy.html")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onPremiumClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = LocalConfiguration.current
    val sheetHeight = (configuration.screenHeightDp.dp * (3f / 4f))

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 6.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_settings_title),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(16.dp))

            PremiumItem(onClick = onPremiumClick)

            Spacer(Modifier.height(12.dp))

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_privacy,
                text = stringResource(R.string.feature_settings_privacy_policy),
                onClick = { openPrivacyPolicy(context) },
            )

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_license,
                text = stringResource(R.string.feature_settings_licenses),
                onClick = { /* TODO */ },
            )

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_brand,
                text = stringResource(R.string.feature_settings_brand_guidelines),
                onClick = { /* TODO */ },
            )

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_feedback,
                text = stringResource(R.string.feature_settings_feedback),
                onClick = { /* TODO */ },
            )

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_share,
                text = stringResource(R.string.feature_settings_share),
                onClick = { shareApp(context) },
            )

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_logout,
                text = stringResource(R.string.feature_settings_logout),
                onClick = onLogout,
            )

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            )
            Spacer(Modifier.height(6.dp))

            SettingsActionItem(
                iconRes = DesignR.drawable.ic_delete,
                text = "Delete my account",
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PremiumItem(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val brush = Brush.horizontalGradient(listOf(Orange, LightOrange))
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(brush)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_premium),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Premium",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun SettingsActionItem(
    @DrawableRes iconRes: Int,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

private const val TAG = "OpenUrl"

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

fun openUrlRobust(
    context: Context,
    url: String,
) {
    val uri = url.toUri()
    val activity = context.findActivity()

    val customTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()

    // Mets la data explicitement sur l’intent (plus fiable que launchUrl seul)
    val intent = customTabsIntent.intent.apply {
        data = uri

        // Si on n’a pas d’Activity, on doit lancer dans une nouvelle task
        if (activity == null) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // (optionnel) forcer Chrome si tu veux, sinon commente cette ligne
        // `package` = "com.android.chrome"
    }

    val canHandle = intent.resolveActivity(context.packageManager) != null
    Log.d(TAG, "activity=${activity != null} canHandle=$canHandle intent=$intent")

    if (canHandle) {
        // Lancer via Activity si possible
        (activity ?: context).startActivity(intent)
    } else {
        // Fallback navigateur classique
        val fallback =
            Intent(Intent.ACTION_VIEW, uri).apply {
                if (activity == null) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        if (fallback.resolveActivity(context.packageManager) != null) {
            (activity ?: context).startActivity(fallback)
        } else {
            Log.e(TAG, "No app can handle this URL: $url")
        }
    }
}

fun openInExternalBrowser(
    context: Context,
    url: String,
) {
    val uri = url.toUri()
    val pm = context.packageManager

    val base = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)

    val candidates =
        pm
            .queryIntentActivities(base, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != context.packageName } // <-- exclut ton app

    val chosenPackage = candidates.firstOrNull()

    Log.d("OpenUrl", "browser candidates=$candidates chosen=$chosenPackage")

    val intent =
        if (chosenPackage != null) {
            base.setPackage(chosenPackage)
        } else {
            base // fallback: Android choisira
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent)
}
