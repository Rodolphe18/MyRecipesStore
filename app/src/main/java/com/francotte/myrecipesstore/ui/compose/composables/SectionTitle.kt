package com.francotte.myrecipesstore.ui.compose.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitle(title: String, count: Int?, onOpenMore: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp)
            .clickable(onClick = { onOpenMore(title) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f))
        {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = count.toString(),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .align(Alignment.Bottom),
                color = Color.Black
            )
        }
        Icon(
            modifier = Modifier.weight(0.15f),
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = title,
            tint = Color.Black
        )
    }
}
