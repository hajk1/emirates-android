package me.haj1.loginapp.util

interface TimeProvider {
    fun currentTimeMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}