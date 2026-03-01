package com.duelup.app.di

import com.duelup.app.data.local.SessionManager
import com.duelup.app.data.remote.socket.SocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    fun provideSocketManager(
        sessionManager: SessionManager,
        json: Json
    ): SocketManager {
        return SocketManager(sessionManager, json)
    }
}
