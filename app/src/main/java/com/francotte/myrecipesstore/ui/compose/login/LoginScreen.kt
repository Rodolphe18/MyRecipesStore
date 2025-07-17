package com.francotte.myrecipesstore.ui.compose.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.ui.compose.composables.CustomButton
import com.francotte.myrecipesstore.ui.compose.composables.CustomTextField
import com.francotte.myrecipesstore.ui.compose.composables.PasswordField
import com.francotte.myrecipesstore.ui.compose.composables.whiteYellowVerticalGradient
import com.francotte.myrecipesstore.ui.theme.Orange

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onRegister: () -> Unit,
    onOpenResetPassword: () -> Unit,
    doGoogleLogin: () -> Unit
) {
    var loginUserNameOrMail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    val canConnect by remember { derivedStateOf { loginUserNameOrMail.isNotEmpty() && loginPassword.isNotEmpty() } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 30.dp, start = 16.dp, end= 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.club_login_page_join_description),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        ButtonGoogle(
            onClick = doGoogleLogin,
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.continue_with_google)
        )

        Spacer(modifier = Modifier.height(16.dp))
//        Button(
//            onClick = { viewModel.deleteAllUsers() },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//        ) {
//            Text("Delete All Users")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ou",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            loginUserNameOrMail,
            { loginUserNameOrMail = it },
            label = "Username or Email",
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(loginPassword, { loginPassword = it })
        Text(
            text = stringResource(id = R.string.sign_in_forgotten_password),
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable { onOpenResetPassword() },
            textDecoration = TextDecoration.Underline,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomButton(onClick = { viewModel.loginWithMailAndPassword(loginUserNameOrMail, loginPassword) },
            enabled = canConnect, contentText = R.string.sign_in_connexion_button)
        Text(
            text = stringResource(id = R.string.onboarding_account_creation),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .clickable { onRegister() },
            color = Orange.copy(0.75f),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}


@Composable
fun ButtonGoogle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_google),
            contentDescription = "Google",
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurface)
    }
}

