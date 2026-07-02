package com.francotte.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.designsystem.component.CustomButton
import com.francotte.designsystem.theme.Lora
import com.francotte.designsystem.theme.Orange
import com.francotte.designsystem.theme.Playfair
import com.francotte.ui.CustomTextField
import com.francotte.ui.DeviceMode
import com.francotte.ui.PasswordField
import com.francotte.ui.rememberDeviceMode
import com.francotte.ui.favButtonDimension

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    val mode = rememberDeviceMode()
    val dimension = remember(mode) { googleButtonDimension(mode) }
    var loginUserNameOrMail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    val canConnect by remember { derivedStateOf { loginUserNameOrMail.isNotEmpty() && loginPassword.isNotEmpty() } }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 30.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (mode == DeviceMode.PhonePortrait) {
            Text(
                text = stringResource(id = R.string.club_login_page_join_description),
                textAlign = TextAlign.Center,
                fontFamily = Playfair,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (mode == DeviceMode.PhonePortrait) {
            ButtonGoogle(
                modifier = Modifier.fillMaxWidth(),
                dimension = dimension,
                onClick = { onAction(LoginAction.OnGoogleLoginClick) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                )
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF888888),
                        ),
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                loginUserNameOrMail,
                { loginUserNameOrMail = it },
                label = "Username or Email",
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(password = loginPassword, onPasswordChange = { loginPassword = it })
        } else {
            Row(Modifier.height(IntrinsicSize.Max),verticalAlignment = Alignment.CenterVertically) {
                ButtonGoogle(
                    modifier = Modifier.weight(1f),
                    dimension = dimension,
                    onClick = { onAction(LoginAction.OnGoogleLoginClick) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column (Modifier.weight(1f)){
                    CustomTextField(
                        loginUserNameOrMail,
                        { loginUserNameOrMail = it },
                        label = "Username or Email",
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PasswordField(password = loginPassword, onPasswordChange = { loginPassword = it })
                }
            }
        }
        Text(
            text = stringResource(id = R.string.sign_in_forgotten_password),
            modifier =
                Modifier
                    .padding(top = 10.dp)
                    .clickable { onAction(LoginAction.OnResetPasswordClick) },
            textDecoration = TextDecoration.Underline,
            fontFamily = Lora,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomButton(
            onClick = { onAction(LoginAction.OnLoginClick(loginUserNameOrMail, loginPassword)) },
            enabled = canConnect && !state.isLoading,
            contentText = R.string.sign_in_connexion_button,
        )
        Text(
            text = stringResource(id = R.string.onboarding_account_creation),
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .clickable { onAction(LoginAction.OnRegisterClick) },
            color = Orange.copy(0.75f),
            style =
                TextStyle(
                    fontFamily = Lora,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                ),
            fontSize = 16.sp,
        )
    }
}

@Composable
fun ButtonGoogle(
    modifier: Modifier = Modifier,
    dimension: GoogleButtonDimension,
    onClick: () -> Unit
) {
    Row(
        modifier =
            modifier
                .height(dimension.height)
                .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
                .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_google),
            contentDescription = "Google",
            modifier = Modifier.size(dimension.imageSize),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Google", color = MaterialTheme.colorScheme.onSurface)
    }
}

@Immutable
data class GoogleButtonDimension(
    val imageSize:Dp,
    val height: Dp,
    val ratio: Float,
)

fun googleButtonDimension(mode: DeviceMode): GoogleButtonDimension = when (mode) {
    DeviceMode.PhonePortrait -> GoogleButtonDimension(imageSize = 28.dp, height = 56.dp, ratio = 5f)
    DeviceMode.PhoneLandscape -> GoogleButtonDimension(imageSize = 40.dp,height = 65.dp, ratio = 2.5f)
    DeviceMode.TabletPortrait -> GoogleButtonDimension(imageSize = 40.dp,height = 100.dp, ratio = 5f)
    DeviceMode.TabletLandscape -> GoogleButtonDimension(imageSize = 40.dp,height = 120.dp, ratio = 1f)
}
