package me.haj1.loginapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.haj1.loginapp.network.NetworkMonitor
import me.haj1.loginapp.repository.AuthRepository
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .onEach { isOnline ->
                    if (!isOnline) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "No internet connection",
                            )
                        }
                    } else {
                        _uiState.update { it.copy() }
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

        // Optional: double-check here (defensive)
        val isOnline = networkMonitor.isOnline.replayCache.lastOrNull() ?: false
        if (!isOnline) {
            _uiState.update { it.copy(errorMessage = "No internet") }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        val result = authRepository.login(state.userName, state.password)
        viewModelScope.launch {
            result.fold(
                onSuccess = { token ->
                    authRepository.resetFailureCount()
                    _uiState.update { it.copy(navigateToHome = true, isLoading = false) }
                },
                onFailure = {
                    authRepository.incrementFailureCount()
                    val failures = authRepository.getFailureCount()
                    if (failures >= 3) {
                        val lockoutUntil = System.currentTimeMillis() + 5 * 60 * 1000 // 5 min
                        authRepository.setLockoutUntil(lockoutUntil)
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
        viewModelScope.launch {
            while (true) {
                val remaining = (until - System.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    _uiState.update { it.copy(isLockedOut = false, lockoutSecondsRemaining = 0) }
                    break
                }
                _uiState.update { it.copy(isLockedOut = true, lockoutSecondsRemaining = remaining) }
                delay(1000)
            }
        }
    }
}