# Login App - Android (Kotlin, Jetpack Compose + MVVM)

A minimal login screen implementation demonstrating modern Android development practices.

## Features

- ✅ Input validation with button enable/disable
- ✅ Successful login triggers navigation
- ✅ Failed login with attempt tracking
- ✅ Account lockout after 3 failures (5 minutes)
- ✅ Offline detection with error message
- ✅ Remember Me token persistence

## Architecture

```
app/src/main/java/me/haj1/loginapp/
├── di/
│   └── ApplicationDI.kt          # Hilt dependency injection module
├── network/
│   ├── NetworkMonitor.kt         # Network connectivity interface
│   └── NetworkMonitorImpl.kt     # ConnectivityManager implementation
├── repository/
│   ├── AuthRepository.kt         # Authentication interface
│   └── AuthRepositoryImpl.kt     # SharedPreferences implementation
├── ui/
│   ├── login/
│   │   ├── LoginScreen.kt        # Compose UI
│   │   ├── LoginUiState.kt       # UI state data class
│   │   └── LoginViewModel.kt     # MVVM ViewModel
│   ├── theme/
│   │   └── ...                   # Material3 theming
│   └── MainActivity.kt           # Entry point
├── util/
│   └── TimeProvider.kt           # Time abstraction for testing
└── MyApplication.kt              # Hilt application class
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with StateFlow
- **DI**: Hilt
- **Testing**: JUnit4, Mockito-Kotlin, Truth, Turbine

## State Management

Uses `StateFlow` for reactive UI state management:

```kotlin
data class LoginUiState(
    val userName: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLockedOut: Boolean = false,
    val lockoutSecondsRemaining: Long = 0L,
    val navigateToHome: Boolean = false,
)
```

## Testing

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

Run instrumented tests:

```bash
./gradlew connectedDebugAndroidTest
```

### Test Coverage

| Test                                                  | Description                        |
|-------------------------------------------------------|------------------------------------|
| `login button disabled when fields are invalid`       | Validation enables/disables button |
| `successful login triggers navigation`                | Success → navigation event         |
| `failed login increments failure count`               | Error increments failure count     |
| `three failed attempts trigger lockout`               | Lockout after 3 failures           |
| `offline state shows message and prevents login call` | Offline handling                   |
| `remember me checked saves token`                     | Token persistence                  |
| `remember me unchecked clears token`                  | Token clearing                     |

## Test Credentials

For demo purposes:

- Username: `kayvan`
- Password: `123456`

## Dependencies

See `app/build.gradle.kts` for full dependency list.

Key dependencies:

- `androidx.compose.material3` - UI components
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel integration
- `com.google.dagger:hilt-android` - Dependency injection
- `org.mockito.kotlin:mockito-kotlin` - Test mocking
- `com.google.truth:truth` - Test assertions