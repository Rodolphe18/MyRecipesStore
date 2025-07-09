package com.francotte.myrecipesstore.ui.compose.favorites.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.ui.compose.add_recipe.bitmapToUri
import com.francotte.myrecipesstore.ui.compose.favorites.login.LoginViewModel
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBackClick:() ->Unit,viewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    val canConnect by remember { mutableStateOf(registerPassword.isNotBlank() && registerEmail.isNotBlank()) }
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val imageUris = remember { mutableStateListOf<Uri>() }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val launcherGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        imageUris.addAll(it)
    }
    val launcherCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let { bitmap ->
            val uri = bitmapToUri(context, bitmap)
            imageUris.add(uri)
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
       Column(modifier = Modifier.padding(horizontal = 16.dp)) {
           Spacer(modifier = Modifier.height(padding.calculateTopPadding()))

           Row(verticalAlignment = Alignment.CenterVertically) {
               Box(Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                   Icon(
                       imageVector = Icons.Default.Phone,
                       contentDescription = null,
                       tint = Color.Yellow
                   )
               }
               Spacer(Modifier.width(12.dp))
               Text(
                   text = "Add a picture",
                   style = TextStyle(textDecoration = TextDecoration.Underline),
                   color = Orange,
                   fontSize = 18.sp,
                   modifier = Modifier.clickable { showImagePickerDialog = true }
               )
           }
           Spacer(modifier = Modifier.height(16.dp))
           TextField(
               value = registerName,
               onValueChange = { registerName = it },
               label = { Text(stringResource(id = R.string.sign_up_name)) },
               modifier = Modifier
                   .fillMaxWidth()
                   .clip(RoundedCornerShape(12.dp))
           )
           Spacer(modifier = Modifier.height(16.dp))
           TextField(
               value = registerEmail,
               onValueChange = { registerEmail = it },
               label = { Text(stringResource(id = R.string.sign_up_email)) },
               modifier = Modifier
                   .fillMaxWidth()
                   .clip(RoundedCornerShape(12.dp))
           )

           Spacer(modifier = Modifier.height(16.dp))
           TextField(
               value = registerPassword,
               onValueChange = { registerPassword = it },
               label = { Text(text = stringResource(id = R.string.sign_up_login)) },
               visualTransformation = PasswordVisualTransformation(),
               keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
               modifier = Modifier
                   .fillMaxWidth()
                   .clip(RoundedCornerShape(12.dp))
           )

           Spacer(modifier = Modifier.height(24.dp))

           Button(
               onClick = {
                   viewModel.createUser(
                       AuthRequest(
                           username = registerEmail,
                           password = registerPassword
                       )
                   )
               },
               enabled = canConnect,
               modifier = Modifier.fillMaxWidth().height(46.dp)
           ) {
               Text(text = "Subscribe")
           }
       }
        if (showImagePickerDialog) {
            AlertDialog(
                onDismissRequest = { showImagePickerDialog = false },
                title = { Text(text = "Add a picture") },
                text = {
                    Column {
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
