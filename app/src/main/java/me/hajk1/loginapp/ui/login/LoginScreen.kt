package me.haj1.loginapp.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

/**
 * Login screen composable implementing Material3 design.
 *
 * Features:
 * - Username and password input fields
 * - Remember Me checkbox for token persistence
 * - Login button with loading state
 * - Error message display
 * - Lockout countdown display
 *
 * Test tags are provided for UI testing:
 * - et_userName: Username input field
 * - et_password: Password input field
 * - cb_rememberMe: Remember Me checkbox
 * - btn_login: Login button
 * - tv_error: Error message text
 * - tv_lockout: Lockout message text
 *
 * @param modifier Modifier for the root layout
 * @param viewModel LoginViewModel instance (injected by Hilt)
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UsernameField(
            value = uiState.userName,
            onValueChange = viewModel::onUserNameChange,
            enabled = !uiState.isLoading && !uiState.isLockedOut
        )

        Spacer(Modifier.height(16.dp))

        PasswordField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            enabled = !uiState.isLoading && !uiState.isLockedOut
        )

        Spacer(Modifier.height(16.dp))

        RememberMeCheckbox(
            checked = uiState.rememberMe,
            onCheckedChange = viewModel::onRememberMeChange,
            enabled = !uiState.isLoading && !uiState.isLockedOut
        )

        Spacer(Modifier.height(24.dp))

        LoginButton(
            onClick = viewModel::login,
            enabled = uiState.isLoginButtonEnabled,
            isLoading = uiState.isLoading
        )

        ErrorMessage(errorMessage = uiState.errorMessage)

        LockoutMessage(
            isLockedOut = uiState.isLockedOut,
            secondsRemaining = uiState.lockoutSecondsRemaining
        )
    }
}

@Composable
private fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("et_userName"),
        value = value,
        onValueChange = onValueChange,
        label = { Text("Username") },
        enabled = enabled,
        singleLine = true
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("et_password"),
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        enabled = enabled,
        singleLine = true
    )
}

@Composable
private fun RememberMeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier.testTag("cb_rememberMe"),
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Text(
            text = "Remember Me",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_login")
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text("Login")
        }
    }
}

@Composable
private fun ErrorMessage(errorMessage: String?) {
    errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .testTag("tv_error")
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun LockoutMessage(
    isLockedOut: Boolean,
    secondsRemaining: Long
) {
    if (isLockedOut) {
        Text(
            text = "Account locked. Try again in ${secondsRemaining}s",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .testTag("tv_lockout")
                .padding(top = 16.dp)
        )
    }
}