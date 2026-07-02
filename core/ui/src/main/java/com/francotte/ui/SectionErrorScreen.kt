package com.francotte.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SectionErrorScreen(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(modifier = Modifier.size(50.dp), painter = painterResource(id = R.drawable.ic_error), contentDescription = null)
        Text(modifier = Modifier.padding(bottom = 8.dp), text = stringResource(id = R.string.error_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center, color = Color.Black)
        Text(modifier = Modifier.padding(bottom = 16.dp), text = stringResource(id = R.string.error_subtitle), fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.Black)
        Button(
            modifier = Modifier
                .width(120.dp)
                .height(40.dp),
            shape = CircleShape,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(colorResource(R.color.light_yellow)),
        ) {
            Text(stringResource(R.string.error_button_text), fontSize = 12.sp, color = colorResource(R.color.orange), fontWeight = FontWeight.Bold)
        }
    }
}
