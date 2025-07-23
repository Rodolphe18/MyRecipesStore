package com.francotte.myrecipesstore.ui.compose.register

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.ui.compose.add_recipe.bitmapToUri
import com.francotte.myrecipesstore.ui.compose.composables.CustomButton
import com.francotte.myrecipesstore.ui.compose.login.LoginViewModel
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBackClick: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    fun isValidUsername(username: String): Boolean = Regex("^(?=.{6,}\$)(?!.* {2,})(?!.* \$)[^\\n]+\$").matches(username)
    fun isValidEmail(email: String): Boolean =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$").matches(email)
    fun isValidPassword(password: String): Boolean =
        Regex("""^(?=.*[A-Z])(?=.*\d).{6,}$""").matches(
            password
        )
    fun isValidConfirmedPassword(): Boolean = registerPassword == confirmPassword
    val isUsernameValid = remember(registerName) { isValidUsername(registerName) }
    val isEmailValid = remember(registerEmail) { isValidEmail(registerEmail) }
    val isPasswordValid = remember(registerPassword) { isValidPassword(registerPassword) }
    val isConfirmPasswordValid = remember(confirmPassword, registerPassword) { isValidConfirmedPassword() }
    val canConnect by remember {
        derivedStateOf {
            isValidUsername(registerName) && isValidEmail(registerEmail) && isValidPassword(
                registerPassword
            ) && isValidConfirmedPassword()
        }
    }
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var image by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            image = it.firstOrNull()
        }
    val launcherCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            it?.let { bitmap ->
                val uri = bitmapToUri(context, bitmap)
                image = uri
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
                modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding() + 12.dp))
                if (image == null) {
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
                            .size(180.dp)
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
                            model = image
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                RegisterTextField(
                    registerName,
                    { registerName = it },
                    isUsernameValid,
                    "Valid name ✅",
                    "At least 6 caracters",
                    "UserName"
                )
                Spacer(modifier = Modifier.height(12.dp))
                RegisterTextField(
                    registerEmail,
                    { registerEmail = it },
                    isEmailValid,
                    "Valid email ✅",
                    "Email incorrect",
                    "Email"
                )
                Spacer(modifier = Modifier.height(12.dp))
                RegisterPasswordField(
                    registerPassword,
                    { registerPassword = it },
                    isPasswordValid,
                    "Secure password ✅",
                    "Invalid password (1 maj, 1 number, 6 characters min)",
                    "Password"
                )
                Spacer(modifier = Modifier.height(12.dp))
                RegisterPasswordField(
                    confirmPassword,
                    { confirmPassword = it },
                    isConfirmPasswordValid,
                    "Password confirmed successfully",
                    "Password is not the same",
                    "Confirm password"
                )
                Spacer(modifier = Modifier.height(36.dp))
                CustomButton(onClick = {
                    viewModel.createUserWithMailAndPassword(
                        username = registerName,
                        email = registerEmail,
                        password = registerPassword,
                        imageUri = image
                    )
                },
                    enabled = canConnect,
                    contentText =  R.string.subscribe)
                Spacer(modifier = Modifier.height(500.dp))
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

@Composable
fun RegisterTextField(
    text: String,
    onTextChange: (String) -> Unit,
    isTextValid: Boolean,
    textValid: String = "",
    textInvalid: String = "",
    label: String = ""
) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFFF6E8D6), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textStyle = LocalTextStyle.current.copy(color = Color(0xFF6D4C41)),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                decorationBox = { innerTextField ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = label,
                                    color = Color(0xFF6D4C41),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }
        if (text.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isTextValid) textValid else textInvalid,
                color = if (isTextValid) Color(0xFF2E7D32) else Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun RegisterPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordValid: Boolean,
    passwordValid: String = "",
    passwordInvalid: String = "",
    label: String = ""
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFF6E8D6), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textStyle = LocalTextStyle.current.copy(color = Color(0xFF6D4C41)),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            decorationBox = { innerTextField ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (password.isEmpty()) {
                            Text(
                                text = label,
                                color = Color(0xFF6D4C41),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF6D4C41)
                        )
                    }
                }
            }
        )

        if (password.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isPasswordValid) passwordValid else passwordInvalid,
                color = if (isPasswordValid) Color(0xFF2E7D32) else Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}