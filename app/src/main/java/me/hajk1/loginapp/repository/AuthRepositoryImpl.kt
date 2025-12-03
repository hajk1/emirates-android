package me.haj1.loginapp.repository

import android.content.Context
import androidx.core.content.edit
import me.haj1.loginapp.network.NetworkMonitor
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val context: Context,
    private val networkMonitor: NetworkMonitor
) : AuthRepository {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    override fun login(userName: String, password: String): Result<String> {
        val currentlyOnline = networkMonitor.isOnline.replayCache.lastOrNull() ?: false
        if (!currentlyOnline) return Result.failure(IOException("Offline"))
        if (userName != "kayvan") return Result.failure(Exception("Invalid credentials"))
        if (password != "123456") return Result.failure(Exception("Invalid credentials"))
        return Result.success("fake-jwt-token")
    }

    override suspend fun incrementFailureCount() {
        val count = getFailureCount() + 1
        prefs.edit { putInt("failures", count) }
    }

    override suspend fun getFailureCount(): Int = prefs.getInt("failures", 0)
    override suspend fun resetFailureCount() = prefs.edit { remove("failures") }
    override suspend fun setLockoutUntil(timestamp: Long) = prefs.edit {
        putLong(
            "lockout_until",
            timestamp
        )
    }

    override suspend fun getLockoutUntil(): Long? =
        prefs.getLong("lockout_until", 0).takeIf { it > 0 }

}