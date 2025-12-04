package me.haj1.loginapp.repository

/**
 * Repository interface for authentication operations.
 * Handles login, failure tracking, lockout management, and token persistence.
 */
interface AuthRepository {
    /**
     * Attempts to login with the provided credentials.
     * @param userName The username
     * @param password The password
     * @return Result containing auth token on success, or exception on failure
     */
    fun login(userName: String, password: String): Result<String>

    /**
     * Increments the failed login attempt counter.
     */
    suspend fun incrementFailureCount()

    /**
     * Returns the current number of failed login attempts.
     */
    suspend fun getFailureCount(): Int

    /**
     * Resets the failed login attempt counter to zero.
     */
    suspend fun resetFailureCount()

    /**
     * Sets the lockout expiration timestamp.
     * @param timestamp Unix timestamp in milliseconds when lockout expires
     */
    suspend fun setLockoutUntil(timestamp: Long)

    /**
     * Returns the lockout expiration timestamp, or null if not locked out.
     */
    suspend fun getLockoutUntil(): Long?

    /**
     * Persists the auth token for "Remember Me" functionality.
     * @param token The auth token to persist, or null to clear
     */
    suspend fun saveToken(token: String?)

    /**
     * Returns the persisted auth token, or null if none saved.
     */
    suspend fun getSavedToken(): String?
}