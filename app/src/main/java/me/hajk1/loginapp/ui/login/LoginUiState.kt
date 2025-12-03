package me.haj1.loginapp.ui.login

data class LoginUiState(
    val userName: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLockedOut: Boolean = false,
    val lockoutTimeRemaining: Long = 0L, // seconds
    val navigateToHome: Boolean = false,
    val lockoutSecondsRemaining: Long = 0L,
) {
    val isLoginButtonEnabled: Boolean
        get() = userName.isNotBlank()
                && password.length >= 6
                && !isLoading
                && !isLockedOut
}