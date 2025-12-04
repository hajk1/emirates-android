package me.haj1.loginapp.repository

import android.content.Context
import androidx.core.content.edit
import me.haj1.loginapp.network.NetworkMonitor
import java.io.IOException
import javax.inject.Inject

/**
 * Implementation of [AuthRepository] using SharedPreferences for persistence.
 *
 * @property context Application context for SharedPreferences access
 * @property networkMonitor Network connectivity monitor
 */
class AuthRepositoryImpl @Inject constructor(
    private val context: Context,
    private val networkMonitor: NetworkMonitor
) : AuthRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_FAILURES = "failures"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    override fun login(userName: String, password: String): Result<String> {
        val currentlyOnline = networkMonitor.isOnline.replayCache.lastOrNull() ?: false
        if (!currentlyOnline) return Result.failure(IOException("Offline"))

        // Demo validation - replace with actual API call in production
        if (userName != "kayvan") return Result.failure(Exception("Invalid credentials"))
        if (password != "123456") return Result.failure(Exception("Invalid credentials"))

        return Result.success("fake-jwt-token")
    }

    override suspend fun incrementFailureCount() {
        val count = getFailureCount() + 1
        prefs.edit { putInt(KEY_FAILURES, count) }
    }

    override suspend fun getFailureCount(): Int = prefs.getInt(KEY_FAILURES, 0)

    override suspend fun resetFailureCount() = prefs.edit { remove(KEY_FAILURES) }

    override suspend fun setLockoutUntil(timestamp: Long) = prefs.edit {
        putLong(KEY_LOCKOUT_UNTIL, timestamp)
    }

    override suspend fun getLockoutUntil(): Long? =
        prefs.getLong(KEY_LOCKOUT_UNTIL, 0).takeIf { it > 0 }

    override suspend fun saveToken(token: String?) {
        prefs.edit {
            if (token != null) {
                putString(KEY_AUTH_TOKEN, token)
            } else {
                remove(KEY_AUTH_TOKEN)
            }
        }
    }

    override suspend fun getSavedToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
}