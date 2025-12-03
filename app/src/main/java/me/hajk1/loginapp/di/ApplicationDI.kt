package me.haj1.loginapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.haj1.loginapp.network.NetworkMonitor
import me.haj1.loginapp.network.NetworkMonitorImpl
import me.haj1.loginapp.repository.AuthRepository
import me.haj1.loginapp.repository.AuthRepositoryImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ApplicationDI {
    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitorImpl(context)

    @Provides
    @Singleton
    fun provideRepository(
        networkMonitor: NetworkMonitor,
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepositoryImpl(
        context, networkMonitor
    )


}