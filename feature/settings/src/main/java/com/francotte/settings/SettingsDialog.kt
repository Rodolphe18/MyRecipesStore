package com.francotte.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties


@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onPremiumClick:()->Unit,
    onShareApp: () -> Unit,
    onDeleteAccount:()->Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .widthIn(max = screenWidth - 40.dp)
            .padding(16.dp),
        containerColor = Color.White,
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.feature_settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), thickness = 1.dp)

                SettingsActionItem(
                    icon = Icons.Default.Money,
                    text = "PREMIUM",
                    onClick = { onPremiumClick() }
                )
                HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), thickness = 1.dp)

                SettingsActionItem(
                    icon = Icons.Default.Lock,
                    text = stringResource(R.string.feature_settings_privacy_policy),
                    onClick = { /* TODO */ }
                )

                SettingsActionItem(
                    icon = Icons.Default.Build,
                    text = stringResource(R.string.feature_settings_licenses),
                    onClick = { /* TODO */ }
                )

                SettingsActionItem(
                    icon = Icons.Default.Star,
                    text = stringResource(R.string.feature_settings_brand_guidelines),
                    onClick = { /* TODO */ }
                )

                SettingsActionItem(
                    icon = Icons.Default.Warning,
                    text = stringResource(R.string.feature_settings_feedback),
                    onClick = { /* TODO */ }
                )

                SettingsActionItem(
                    icon = Icons.Default.Share,
                    text = stringResource(R.string.feature_settings_share),
                    onClick = onShareApp
                )

                SettingsActionItem(
                    icon = Icons.Default.Close,
                    text = stringResource(R.string.feature_settings_logout),
                    onClick = onLogout
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsActionItem(
                    icon = Icons.Default.Delete,
                    text = "Delete my account",
                    onClick = onDeleteAccount,
                    tint = Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = Color.Black,
    fontWeight: FontWeight= FontWeight.Normal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            fontWeight = fontWeight,
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}
