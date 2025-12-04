package me.haj1.loginapp.network

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface for monitoring network connectivity status.
 * Implementations should emit connectivity changes in real-time.
 */
interface NetworkMonitor {
    /**
     * A [SharedFlow] that emits `true` when the device has internet connectivity,
     * and `false` when offline. Uses replay=1 to provide the latest status to new collectors.
     */
    val isOnline: SharedFlow<Boolean>
}