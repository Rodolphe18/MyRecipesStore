package com.francotte.myrecipesstore.ui.compose.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButton(onClick:()->Unit, enabled: Boolean, @StringRes contentText: Int){
    Button(
        onClick = { onClick() },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6D4C41), // Café brun
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFBCAAA4), // plus pâle
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = stringResource(id = contentText),
            fontSize = 18.sp,
            color = Color.White
        )
    }
}