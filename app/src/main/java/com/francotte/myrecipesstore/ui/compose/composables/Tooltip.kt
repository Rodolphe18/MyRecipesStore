package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@Composable
fun CustomTooltip(
    fullText: String,
    modifier: Modifier = Modifier
) {
    val displayedText = remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(true) }

    // Écriture progressive quand visible devient true
    LaunchedEffect(Unit) {
        if (visible) {
            delay(500)
            displayedText.value = ""
            val delayPerChar = 3000L / fullText.length
            for (i in fullText.indices) {
                displayedText.value = fullText.substring(0, i + 1)
                delay(delayPerChar)
            }
            delay(300L)
            visible = false
        } else {
            displayedText.value = ""
        }
    }

    if (visible) {
        val triangleHeight = 12.dp
        val cornerRadius = 8.dp

        Box(
            modifier = modifier
                .padding(8.dp)
                .height(60.dp + triangleHeight)
                .width(240.dp)
                .drawBehind {
                    val tooltipHeight = size.height - triangleHeight.toPx()
                    val width = size.width
                    val height = size.height

                    val trianglePath = Path().apply {
                        moveTo(width / 2f - triangleHeight.toPx(), tooltipHeight)
                        lineTo(width / 2f, height)
                        lineTo(width / 2f + triangleHeight.toPx(), tooltipHeight)
                        close()
                    }

                    val brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF8541A), Color(0xFFFF6F00)),
                        start = Offset.Zero,
                        end = Offset(width, height)
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(0f, 0f),
                        size = Size(width, tooltipHeight),
                        cornerRadius = CornerRadius(cornerRadius.toPx())
                    )
                    drawPath(trianglePath, brush = brush)
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = displayedText.value,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp, end = 12.dp, start = 12.dp).align(Alignment.Center)
            )
        }
    }
}
