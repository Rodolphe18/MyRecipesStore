package com.francotte.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
    title: String? = null,
    actionIcon: ImageVector? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    actionIconContentDescription: String? = "",
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onActionClick: () -> Unit = {},
    onNavigationClick: () -> Unit = {},
    navigationIconEnabled: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    profileImage: String? = null,
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            if (title != null) Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) else Text(
                text = stringResource(id = titleRes ?: 0),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        },
        actions = {
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        modifier = Modifier
                            .size(32.dp),
                        imageVector = actionIcon,
                        contentDescription = actionIconContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        navigationIcon = {
            if (navigationIconEnabled) {
                if (profileImage != null && profileImage != "https://app.myrecipesstore18.com/null") {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = profileImage
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .offset(x=12.dp)
                            .size(45.dp)
                            .clip(CircleShape)
                            .clickable {
                                onNavigationClick()
                            }
                    )
                } else {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            modifier = Modifier.size(35.dp),
                            imageVector = navigationIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        colors = colors,
        modifier = modifier,
    )
}