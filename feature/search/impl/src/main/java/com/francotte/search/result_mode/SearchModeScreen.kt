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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.designsystem.component.TopAppBar
import com.francotte.designsystem.theme.Lora
import com.francotte.designsystem.theme.SearchItemColor1
import com.francotte.designsystem.theme.SearchItemColor2
import com.francotte.designsystem.theme.SearchItemColor3
import com.francotte.ui.nbIngredientsColumns
import com.francotte.ui.rememberDeviceMode
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectionGrid(
    state: SearchModeState,
    onAction: (SearchModeAction) -> Unit,
) {
    val mode = rememberDeviceMode()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = state.title,
                navigationIconEnabled = true,
                onNavigationClick = { onAction(SearchModeAction.OnBackClick) },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(SearchModeAction.OnRefresh) },
                state = pullToRefreshState
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(mode.nbIngredientsColumns),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items) { item ->
                        SelectableChip(Modifier.fillMaxWidth(), item, onClick = { onAction(SearchModeAction.OnItemClick(item)) })
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableChip(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit,
) {
    val backgroundColors =
        listOf(
            SearchItemColor1,
            SearchItemColor2,
            SearchItemColor3,
        )
    val index = (label.hashCode().absoluteValue % backgroundColors.size)
    val randomColor = remember(label) { backgroundColors[index] }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .background(randomColor)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color(0xFF6D4C41),
            fontFamily = Lora,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
