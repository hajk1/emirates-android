package me.haj1.loginapp

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import me.haj1.loginapp.network.NetworkMonitor
import me.haj1.loginapp.repository.AuthRepository
import me.haj1.loginapp.ui.login.LoginViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException


@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository
    private val networkFlow = MutableSharedFlow<Boolean>(replay = 1)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        networkFlow.tryEmit(true) // default online

        authRepository = mock()
        val networkMonitor = object : NetworkMonitor {
            override val isOnline = networkFlow
        }

        viewModel = LoginViewModel(
            authRepository = authRepository,
            networkMonitor = networkMonitor,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun ` login button disabled when fields are invalid`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isLoginButtonEnabled).isFalse()

            viewModel.onUserNameChange("")
            assertThat(awaitItem().isLoginButtonEnabled).isFalse() // password missing

            viewModel.onPasswordChange("123456")
            assertThat(awaitItem().isLoginButtonEnabled).isTrue()
        }
    }

    @Test
    fun `successful login triggers navigation and saves token when remember me is checked`() =
        runTest {
            whenever(authRepository.login(any(), any())).thenReturn(Result.success("token123"))

            viewModel.onUserNameChange("username")
            viewModel.onPasswordChange("123456")
            viewModel.login()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.navigateToHome).isTrue()
            // Token saved only if remember me checked
        }

    @Test
    fun `failed login increments failure count and shows error`() = runTest {
        whenever(
            authRepository.login(
                any(),
                any()
            )
        ).thenReturn(Result.failure(IOException("Invalid")))
        whenever(authRepository.getFailureCount()).thenReturn(1)

        viewModel.onUserNameChange("fiil")
        viewModel.onPasswordChange("wrong")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("Attempt 1/3")
        org.mockito.kotlin.verify(authRepository).incrementFailureCount()
    }

    @Test
    fun `three failed attempts trigger lockout with timer`() = runTest {
        whenever(authRepository.login(any(), any())).thenReturn(Result.failure(Exception("Fail")))
        whenever(authRepository.getFailureCount()).thenReturn(1, 2, 3)

        viewModel.onUserNameChange("fail")
        viewModel.onPasswordChange("wrong")

        repeat(3) {
            viewModel.login()
            advanceUntilIdle()
        }

        val state = viewModel.uiState.value
        assertThat(state.isLockedOut).isTrue()
        assertThat(state.lockoutSecondsRemaining).isGreaterThan(0L)
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun `offline state shows message and prevents login call`() = runTest {
        networkFlow.emit(false)

        viewModel.onUserNameChange("username")
        viewModel.onPasswordChange("password")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isEqualTo("No internet connection")
        assertThat(state.isLoading).isFalse()
        org.mockito.kotlin.verify(authRepository, org.mockito.kotlin.never()).login(any(), any())
    }

}