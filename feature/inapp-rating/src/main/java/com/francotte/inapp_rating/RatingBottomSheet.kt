package com.francotte.inapp_rating

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RatingBottomSheetHost(inAppRatingManager: InAppRatingManager) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var evaluated by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val localActivity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        if (evaluated) return@LaunchedEffect
        evaluated = true

        val shouldShow = inAppRatingManager.shouldShowCustomRatingBottomSheet()
        if (shouldShow) {
            inAppRatingManager.inAppRatingDialogShown()
            showSheet = true
        }
    }

    if (showSheet) {
        RatingBottomSheet(
            onRate = {
                showSheet = false
                inAppRatingManager.setHasBeenRatedOrNotAskAgainToTrue()
                val safeActivity = localActivity ?: return@RatingBottomSheet
                scope.launch {
                    inAppRatingManager.requestReviewOnPlayStore(safeActivity)
                }
            },
            onLater = { showSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingBottomSheet(
    onRate: () -> Unit,
    onLater: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onLater) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu apprécies l’app ?",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Une note sur le Play Store nous aide énormément 🙂",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onLater
                ) {
                    Text("Plus tard")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onRate
                ) {
                    Text("Noter")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

