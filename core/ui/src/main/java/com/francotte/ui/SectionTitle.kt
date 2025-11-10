package com.francotte.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SectionTitle(modifier: Modifier= Modifier,title: String, count: Int?, showNavIcon:Boolean= true, paddingStart: Dp= 18.dp, onOpenMore: (String) -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingStart)
            .testTag("SectionTitle_$title")
            .semantics { contentDescription = "SectionTitle_$title" }
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(10.dp))
            count?.let { Text(
                text = it.toString(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .align(Alignment.Bottom),
                color = MaterialTheme.colorScheme.primary
            ) }

        }
        if (showNavIcon) {
            Icon(
                modifier = Modifier.weight(0.15f),
                painter = painterResource(R.drawable.ic_more),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
