package me.haj1.loginapp.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.testTag("et_userName"),
            value = uiState.userName,
            onValueChange = viewModel::onUserNameChange,
            label = { Text("UserName") },
            enabled = !uiState.isLoading && !uiState.isLockedOut
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.testTag("et_password"),
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !uiState.isLoading && !uiState.isLockedOut
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::login,
            enabled = uiState.isLoginButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_login")
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text("Login")
            }
        }

        uiState.errorMessage?.let {
            Text(
                it, color = Color.Red, modifier = Modifier
                    .testTag("tv_error")
                    .padding(top = 16.dp)
            )
        }

        if (uiState.isLockedOut) {
            Text(
                "Account locked. Try again in ${uiState.lockoutSecondsRemaining}s",
                color = Color.Red,
                modifier = Modifier
                    .testTag("tv_lockout")
                    .padding(top = 16.dp)
            )
        }
    }
}