package com.francotte.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francotte.designsystem.R


@Composable
fun ErrorScreen(modifier: Modifier = Modifier, retry: (() -> Unit)) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp).offset(y=20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(com.francotte.ui.R.drawable.ic_error), null)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(com.francotte.ui.R.string.error_title), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(com.francotte.ui.R.string.error_subtitle),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = retry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(com.francotte.ui.R.string.error_button_text))
        }
    }
}