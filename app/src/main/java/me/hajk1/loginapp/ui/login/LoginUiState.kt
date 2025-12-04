package me.haj1.loginapp.ui.login

/**
 * Represents the UI state for the Login screen.
 *
 * @property userName The entered username
 * @property password The entered password
 * @property rememberMe Whether to persist the auth token after successful login
 * @property isLoading Whether a login request is in progress
 * @property errorMessage Error message to display, null if no error
 * @property isLockedOut Whether the account is locked due to failed attempts
 * @property lockoutTimeRemaining Deprecated - use lockoutSecondsRemaining
 * @property navigateToHome Whether to navigate to home screen after successful login
 * @property lockoutSecondsRemaining Seconds remaining until lockout expires
 */
data class LoginUiState(
    val userName: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLockedOut: Boolean = false,
    val lockoutTimeRemaining: Long = 0L,
    val navigateToHome: Boolean = false,
    val lockoutSecondsRemaining: Long = 0L,
) {
    /**
     * Login button is enabled when:
     * - Username is not blank
     * - Password is at least 6 characters
     * - No login request is in progress
     * - Account is not locked out
     */
    val isLoginButtonEnabled: Boolean
        get() = userName.isNotBlank()
                && password.length >= 6
                && !isLoading
                && !isLockedOut
}