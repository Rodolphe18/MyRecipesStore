package com.francotte.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onPremiumClick: () -> Unit,
    onShareApp: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val sheetHeight = (configuration.screenHeightDp.dp * (3f / 4f))

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.feature_settings_title),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            SettingsButton(text = "Premium", imageVector = Icons.Outlined.Diamond, backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), contentColor = Color.White, onClick = onPremiumClick)

            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(8.dp))
            SettingsButton(text = "Delete my account", height = 52.dp, imageVector = Icons.Default.Delete, contentColor = Color.Red.copy(alpha = 0.7f),borderColor = Color.Red.copy(alpha = 0.7f), backgroundColor = Color.White, onClick = onDeleteClick)


            Spacer(Modifier.height(8.dp))
        }
    }
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
            fontSize = 18.sp,
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}

@Composable
fun SettingsButton(
    text: String,
    height: Dp =56.dp,
    imageVector: ImageVector,
    borderColor: Color=Color.Transparent,
    backgroundColor:Color,
    contentColor:Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(BorderStroke(1.dp,borderColor),shape)
            .clickable(onClick = onClick),
        color = backgroundColor,
        contentColor = contentColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector =  imageVector,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        }
    }
}
