package com.francotte.myrecipesstore.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int?=null,
    title:String? =null,
    actionIcon: ImageVector?=null,
    navigationIcon:ImageVector=Icons.Default.ArrowBack,
    actionIconContentDescription: String?="",
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onActionClick: () -> Unit = {},
    onNavigationClick:() -> Unit = {},
    navigationIconEnabled:Boolean= false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = { if(title !=null) Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) else Text(text = stringResource(id = titleRes ?: 0), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) },
        actions = {
            if (actionIcon != null) {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }},
        navigationIcon = {
            if(navigationIconEnabled) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        colors = colors,
        modifier = modifier,
    )
}