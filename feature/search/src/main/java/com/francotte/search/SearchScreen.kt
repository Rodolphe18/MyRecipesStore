package com.francotte.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Composable
fun SearchModeSelectionScreen(onSearchModeSelected: (SearchMode) -> Unit) {

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How would you like to \n search recipes ?", textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)

        SearchModeButton("By ingredients", Icons.Default.ThumbUp) {
            onSearchModeSelected(SearchMode.INGREDIENTS)
        }
        SearchModeButton("By country", Icons.Default.Notifications) {
            onSearchModeSelected(SearchMode.COUNTRY)
        }
    //    AdMobBanner(height = 100.dp)

    }
}

@Composable
fun SearchModeButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.75f),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)), // Orange foncé
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Icon(icon, contentDescription = null, Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Serializable
enum class SearchMode(val title:String) { INGREDIENTS("Ingredients"), COUNTRY("Countries") }


