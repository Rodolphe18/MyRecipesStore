package com.francotte.myrecipesstore.ui.compose.reset.reset

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable


@Serializable
data class ResetRoute(val token: String)

fun NavController.navigateToResetPasswordScreen(token: String, navOptions: NavOptions? = null) {
    this.navigate(route = ResetRoute(token), navOptions)
}


fun NavGraphBuilder.resetPasswordScreen() {
    composable<ResetRoute> { backStackEntry ->
        val route = backStackEntry.arguments
        val token = route?.getString("token")

        if (token != null) {
            ResetPasswordScreen(token = token)
        }
    }
}


@Composable
fun ResetPasswordScreen(token: String, viewModel: ResetPasswordViewModel = hiltViewModel()) {
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Nouveau mot de passe")
        TextField(value = password, onValueChange = { password = it })
        Button(onClick = { viewModel.resetPassword(token, password) }) {
            Text("Confirmer")
        }

        if (viewModel.uiState.isNotBlank()) {
            Text(viewModel.uiState, color = if (viewModel.isSuccess) Color.Green else Color.Red)
        }
    }
}