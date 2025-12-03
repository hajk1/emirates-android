package me.haj1.loginapp.repository

interface AuthRepository {

    fun login(userName: String, password: String): Result<String>
    suspend fun incrementFailureCount()
    suspend fun getFailureCount(): Int
    suspend fun resetFailureCount()
    suspend fun setLockoutUntil(timestamp: Long)
    suspend fun getLockoutUntil(): Long?
}