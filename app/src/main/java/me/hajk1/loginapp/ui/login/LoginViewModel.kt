package me.haj1.loginapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.haj1.loginapp.network.NetworkMonitor
import me.haj1.loginapp.repository.AuthRepository
import me.haj1.loginapp.util.TimeProvider
import javax.inject.Inject

/**
 * ViewModel for the Login screen implementing MVVM pattern.
 *
 * Responsibilities:
 * - Manages login UI state
 * - Handles user input validation
 * - Coordinates login flow with repository
 * - Tracks failed attempts and manages lockout
 * - Monitors network connectivity
 * - Persists auth token when "Remember Me" is enabled
 *
 * @property networkMonitor Monitors network connectivity status
 * @property authRepository Handles authentication and persistence
 * @property timeProvider Abstraction for time operations (testable)
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    companion object {
        /** Lockout duration after 3 failed attempts (5 minutes) */
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L

        /** Maximum allowed failed attempts before lockout */
        const val MAX_FAILED_ATTEMPTS = 3
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private var lockoutTimerJob: Job? = null

    init {
        observeNetworkStatus()
    }

    /**
     * Observes network connectivity and updates UI state accordingly.
     * Shows error message when offline, clears it when back online.
     */
    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .onEach { isOnline ->
                    if (!isOnline) {
                        _uiState.update {
                            it.copy(errorMessage = "No internet connection")
                        }
                    } else {
                        _uiState.update { it.copy(errorMessage = null) }
                    }
                }
                .collect()
        }
    }

    /**
     * Updates the username in UI state.
     * Trims whitespace from input.
     */
    fun onUserNameChange(userName: String) {
        _uiState.update { it.copy(userName = userName.trim()) }
    }

    /**
     * Updates the password in UI state.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Updates the "Remember Me" preference in UI state.
     */
    fun onRememberMeChange(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    /**
     * Attempts to login with current credentials.
     *
     * Flow:
     * 1. Validates button is enabled and not already loading
     * 2. Checks network connectivity
     * 3. Calls repository login
     * 4. On success: saves token if Remember Me, navigates to home
     * 5. On failure: increments failure count, triggers lockout if needed
     */
    fun login() {
        val state = uiState.value
        if (!state.isLoginButtonEnabled || state.isLoading || state.isLockedOut) return

        val isOnline = networkMonitor.isOnline.replayCache.lastOrNull() ?: false
        if (!isOnline) {
            _uiState.update { it.copy(errorMessage = "No internet connection") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = authRepository.login(state.userName, state.password)

        viewModelScope.launch {
            result.fold(
                onSuccess = { token ->
                    handleLoginSuccess(token, state.rememberMe)
                },
                onFailure = {
                    handleLoginFailure()
                }
            )
        }
    }

    /**
     * Handles successful login.
     * Saves token if Remember Me is enabled, resets failure count, triggers navigation.
     */
    private suspend fun handleLoginSuccess(token: String, rememberMe: Boolean) {
        if (rememberMe) {
            authRepository.saveToken(token)
        } else {
            authRepository.saveToken(null)
        }
        authRepository.resetFailureCount()
        _uiState.update { it.copy(navigateToHome = true, isLoading = false) }
    }

    /**
     * Handles failed login.
     * Increments failure count and triggers lockout after MAX_FAILED_ATTEMPTS.
     */
    private suspend fun handleLoginFailure() {
        authRepository.incrementFailureCount()
        val failures = authRepository.getFailureCount()

        if (failures >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = timeProvider.currentTimeMillis() + LOCKOUT_DURATION_MS
            authRepository.setLockoutUntil(lockoutUntil)
            _uiState.update { it.copy(isLoading = false) }
            startLockoutTimer(lockoutUntil)
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = "Login failed (Attempt $failures/$MAX_FAILED_ATTEMPTS)",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Starts a countdown timer for the lockout period.
     * Updates UI every second with remaining time.
     * Resets lockout and failure count when timer expires.
     */
    private fun startLockoutTimer(until: Long) {
        lockoutTimerJob?.cancel()
        lockoutTimerJob = viewModelScope.launch {
            while (true) {
                val remaining = (until - timeProvider.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    authRepository.resetFailureCount()
                    _uiState.update { it.copy(isLockedOut = false, lockoutSecondsRemaining = 0) }
                    break
                }
                _uiState.update { it.copy(isLockedOut = true, lockoutSecondsRemaining = remaining) }
                delay(1000)
            }
        }
    }

    /**
     * Cancels the lockout timer. Used for testing cleanup.
     */
    fun cancelLockoutTimer() {
        lockoutTimerJob?.cancel()
    }
}