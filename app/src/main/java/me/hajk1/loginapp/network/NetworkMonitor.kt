package me.haj1.loginapp.network

import kotlinx.coroutines.flow.SharedFlow

interface NetworkMonitor {
    val isOnline: SharedFlow<Boolean>


}