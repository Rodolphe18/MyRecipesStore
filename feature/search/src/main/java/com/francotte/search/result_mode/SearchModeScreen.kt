package com.francotte.search.result_mode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.designsystem.component.TopAppBar
import com.francotte.designsystem.theme.SearchItemColor1
import com.francotte.designsystem.theme.SearchItemColor2
import com.francotte.designsystem.theme.SearchItemColor3
import com.francotte.search.SearchMode
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectionGrid(
    searchMode: SearchMode,
    items: List<String>,
    onItemSelected: (String, SearchMode) -> Unit,
    onBack: () -> Unit
) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = searchMode.title,
                navigationIconEnabled = true,
                onNavigationClick = onBack,
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    SelectableChip(item, onClick = { onItemSelected(item, searchMode) })
                }
            }
        }
    }
}


@Composable
fun SelectableChip(
    label: String,
    onClick: () -> Unit
) {
    val backgroundColors = listOf(
        SearchItemColor1,
        SearchItemColor2,
        SearchItemColor3
    )
    val index = (label.hashCode().absoluteValue % backgroundColors.size)
    val randomColor = remember(label) { backgroundColors[index] }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .background(randomColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF6D4C41),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}