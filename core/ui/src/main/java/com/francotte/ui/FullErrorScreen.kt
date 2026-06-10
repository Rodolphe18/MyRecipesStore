package com.francotte.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Full-screen error state with no retry button: the user retries by pulling to
 * refresh. Content is vertically + horizontally centered. A single-item
 * `LazyColumn` keeps the host's pull-to-refresh gesture working (a scrollable
 * child is required) while `fillParentMaxSize` gives bounded height for centering.
 */
@Composable
fun FullErrorScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(modifier = Modifier.size(50.dp), painter = painterResource(id = R.drawable.ic_error), contentDescription = null)
                Text(modifier = Modifier.padding(bottom = 8.dp), text = stringResource(id = R.string.error_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center, color = Color.Black)
                Text(text = stringResource(id = R.string.error_pull_to_refresh), fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.Black)
            }
        }
    }
}
