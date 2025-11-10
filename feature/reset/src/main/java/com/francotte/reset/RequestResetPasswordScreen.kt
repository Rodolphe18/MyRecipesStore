package com.francotte.reset

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.designsystem.component.TopAppBar
import com.francotte.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestResetPasswordScreen(viewModel: LoginViewModel = hiltViewModel(), onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val resetState by viewModel.resetState.collectAsState()
    val focus = LocalFocusManager.current

    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = "Password forgotten ?",
                navigationIconEnabled = true,
                navigationIcon = Icons.Filled.Close,
                onNavigationClick = onBack,
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(16.dp)) {
            Text("To obtain a new password, enter the email address \nassociated to your account")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.requestReset(email) }) {
                Text("Send Email")
            }

            resetState?.let {
                when {
                    it.isSuccess -> {
                        Text("Email sent successfully ✅", color = Color.Green)
                        email = ""
                        focus.clearFocus()
                    }

                    it.isFailure -> {
                        Text("An error occured. Try again", color = Color.Red)
                    }
                }
            }
        }
    }
}

