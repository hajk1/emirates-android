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


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    companion object {
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private var lockoutTimerJob: Job? = null

    init {
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

    fun onUserNameChange(userName: String) {
        _uiState.update { it.copy(userName = userName.trim()) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

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
                onSuccess = { _ ->
                    authRepository.resetFailureCount()
                    _uiState.update { it.copy(navigateToHome = true, isLoading = false) }
                },
                onFailure = {
                    authRepository.incrementFailureCount()
                    val failures = authRepository.getFailureCount()
                    if (failures >= 3) {
                        val lockoutUntil = timeProvider.currentTimeMillis() + LOCKOUT_DURATION_MS
                        authRepository.setLockoutUntil(lockoutUntil)
                        _uiState.update { it.copy(isLoading = false) }
                        startLockoutTimer(lockoutUntil)
                    } else {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Login failed (Attempt $failures/3)",
                                isLoading = false
                            )
                        }
                    }
                }
            )
        }
    }

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

    // Expose for testing
    fun cancelLockoutTimer() {
        lockoutTimerJob?.cancel()
    }
}