package me.haj1.loginapp.util

/**
 * Abstraction for time operations to enable testing.
 * Allows injecting fake time in tests while using system time in production.
 */
interface TimeProvider {
    /**
     * Returns the current time in milliseconds since epoch.
     */
    fun currentTimeMillis(): Long
}

/**
 * Production implementation using [System.currentTimeMillis].
 */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}