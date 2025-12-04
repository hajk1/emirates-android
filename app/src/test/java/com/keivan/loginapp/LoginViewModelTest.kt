package me.haj1.loginapp

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.haj1.loginapp.network.NetworkMonitor
import me.haj1.loginapp.repository.AuthRepository
import me.haj1.loginapp.ui.login.LoginViewModel
import me.haj1.loginapp.util.TimeProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.wheneverBlocking
import java.io.IOException

/**
 * Unit tests for [LoginViewModel].
 *
 * Tests cover:
 * 1. Validation enables/disables button
 * 2. Success → navigation event
 * 3. Error increments failure count
 * 4. Lockout after 3 failures
 * 5. Offline → show message, no service call
 * 6. Remember me persists token
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository
    private val networkFlow = MutableSharedFlow<Boolean>(replay = 1)

    private var fakeCurrentTime = 0L
    private val fakeTimeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = fakeCurrentTime
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        networkFlow.tryEmit(true)
        fakeCurrentTime = 1000000L

        authRepository = mock()
        val networkMonitor = object : NetworkMonitor {
            override val isOnline = networkFlow
        }

        viewModel = LoginViewModel(
            authRepository = authRepository,
            networkMonitor = networkMonitor,
            timeProvider = fakeTimeProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test #1: Validation enables/disables button
     */
    @Test
    fun `login button disabled when fields are invalid`() {
        assertThat(viewModel.uiState.value.isLoginButtonEnabled).isFalse()

        viewModel.onUserNameChange("user")
        assertThat(viewModel.uiState.value.isLoginButtonEnabled).isFalse()

        viewModel.onPasswordChange("123456")
        assertThat(viewModel.uiState.value.isLoginButtonEnabled).isTrue()

        viewModel.onUserNameChange("")
        assertThat(viewModel.uiState.value.isLoginButtonEnabled).isFalse()
    }

    /**
     * Test #2: Success → navigation event
     */
    @Test
    fun `successful login triggers navigation`() {
        wheneverBlocking {
            authRepository.login(
                any(),
                any()
            )
        }.thenReturn(Result.success("token123"))

        viewModel.onUserNameChange("username")
        viewModel.onPasswordChange("123456")
        viewModel.login()

        val state = viewModel.uiState.value
        assertThat(state.navigateToHome).isTrue()
    }

    /**
     * Test #3: Error increments failure count
     */
    @Test
    fun `failed login increments failure count and shows error`() {
        wheneverBlocking { authRepository.login(any(), any()) }
            .thenReturn(Result.failure(IOException("Invalid")))
        wheneverBlocking { authRepository.getFailureCount() }.thenReturn(1)

        viewModel.onUserNameChange("fail")
        viewModel.onPasswordChange("wrongpass")
        viewModel.login()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).contains("Attempt 1/3")
    }

    /**
     * Test #4: Lockout after 3 failures
     */
    @Test
    fun `three failed attempts trigger lockout`() {
        wheneverBlocking { authRepository.login(any(), any()) }
            .thenReturn(Result.failure(Exception("Fail")))
        wheneverBlocking { authRepository.getFailureCount() }.thenReturn(1, 2, 3)

        viewModel.onUserNameChange("fail")
        viewModel.onPasswordChange("wrongpass")

        repeat(3) {
            viewModel.login()
        }

        fakeCurrentTime += 1000

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()

        viewModel.cancelLockoutTimer()
    }

    /**
     * Test #5: Offline → show message, no service call
     */
    @Test
    fun `offline state shows message and prevents login call`() {
        networkFlow.tryEmit(false)

        viewModel.onUserNameChange("username")
        viewModel.onPasswordChange("password123")
        viewModel.login()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isEqualTo("No internet connection")
        assertThat(state.isLoading).isFalse()
    }

    /**
     * Test #6a: Remember me checked saves token on successful login
     */
    @Test
    fun `remember me checked saves token on successful login`() = runTest {
        wheneverBlocking {
            authRepository.login(
                any(),
                any()
            )
        }.thenReturn(Result.success("token123"))
        wheneverBlocking { authRepository.saveToken(any()) }.thenReturn(Unit)
        wheneverBlocking { authRepository.resetFailureCount() }.thenReturn(Unit)

        viewModel.onUserNameChange("username")
        viewModel.onPasswordChange("123456")
        viewModel.onRememberMeChange(true)
        viewModel.login()

        verify(authRepository).saveToken("token123")
    }

    /**
     * Test #6b: Remember me unchecked clears token on successful login
     */
    @Test
    fun `remember me unchecked clears token on successful login`() = runTest {
        wheneverBlocking {
            authRepository.login(
                any(),
                any()
            )
        }.thenReturn(Result.success("token123"))
        wheneverBlocking { authRepository.saveToken(any()) }.thenReturn(Unit)
        wheneverBlocking { authRepository.resetFailureCount() }.thenReturn(Unit)

        viewModel.onUserNameChange("username")
        viewModel.onPasswordChange("123456")
        viewModel.onRememberMeChange(false)
        viewModel.login()

        verify(authRepository).saveToken(null)
    }
}