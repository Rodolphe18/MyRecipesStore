package com.francotte.myrecipesstore.ui.compose.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.network.model.CurrentUser
import com.francotte.myrecipesstore.ui.compose.add_recipe.bitmapToUri
import com.francotte.myrecipesstore.ui.compose.composables.whiteYellowVerticalGradient
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.ui.theme.Orange


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    user: CurrentUser?,
    updateCurrentUser: (name: String?, image: Uri?) -> Unit
) {
    val context = LocalContext.current
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val currentName = user?.username
    val currentEmail = user?.email
    val currentImage = user?.image
    var updatedImage by remember { mutableStateOf<Uri?>(null) }
    val focusManager = LocalFocusManager.current
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            updatedImage = it
        }
    val launcherCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            it?.let { bitmap ->
                val uri = bitmapToUri(context, bitmap)
                updatedImage = uri
            }
        }
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = "Register",
                scrollBehavior = topAppBarScrollBehavior,
                navigationIconEnabled = true,
                onNavigationClick = onBackClick
            )
        }
    ) { padding ->
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            visible = true
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight }, // depuis le bas
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }, // vers la gauche
                animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding() + 20.dp))
                if (currentImage == null) {
                    val scale = remember { Animatable(1f) }
                    LaunchedEffect(Unit) {
                        scale.animateTo(
                            targetValue = 1.05f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutLinearInEasing
                            )
                        )
                    }
                    Box(
                        Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Orange.copy(alpha = 0.8f))
                            .clickable { showImagePickerDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                modifier = Modifier.graphicsLayer(
                                    scaleX = scale.value,
                                    scaleY = scale.value
                                ),
                                text = "Click here to \nadd a picture",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                } else {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = if (updatedImage != null) updatedImage else currentImage
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .clickable { showImagePickerDialog = true })
                }
                Spacer(modifier = Modifier.height(16.dp))
                val state = rememberTextFieldState(currentName!!)
                val isValid = state.text.length >= 6

                Column(verticalArrangement = Arrangement.Center) {
                    Text("Username", color = MaterialTheme.colorScheme.onSurface)
                    BasicTextField(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(0xFFF6E8D6), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        decorator = { innerTextField ->
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                innerTextField()
                            }
                        }
                    )
                    if (state.text.isNotEmpty() && state.text.toString() != currentName) {
                        Text(
                            text = if (isValid) "Valid ✅" else "At least 6 characters",
                            color = if (isValid) Color(0xFF81C784) else Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text("Email", color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF6E8D6))
                            .padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = currentEmail!!, color = Color.Black, fontSize = 14.sp)
                    }

                    Text(
                            text = stringResource(R.string.supporting_text_email),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        updateCurrentUser(
                            if (state.text.isNotBlank()) state.text.toString() else currentName,
                            updatedImage
                        )
                        focusManager.clearFocus()
                        state.edit {
                            this.replace(start = 0, end =0, text = "")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(46.dp),
                ) {
                    Text(text = "Update", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                }
            }
            if (showImagePickerDialog) {
                AlertDialog(
                    onDismissRequest = { showImagePickerDialog = false },
                    title = { Text(text = "Add a picture") },
                    text = {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "From camera",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImagePickerDialog = false
                                        launcherCamera.launch()
                                    }
                                    .padding(vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "From gallery",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImagePickerDialog = false
                                        launcherGallery.launch("image/*")
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }
        }
    }
}