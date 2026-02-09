package com.francotte.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun CustomBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().aspectRatio(1f).clip(CircleShape).drawWithContent{
        drawRect(Color.Yellow)
    },contentAlignment = Alignment.Center) {

    }
}


@Preview
@Composable
private fun CustomBoxPrev() {
    CustomBox()
}
