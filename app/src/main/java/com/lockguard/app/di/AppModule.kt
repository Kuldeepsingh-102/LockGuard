package com.lockguard.app.di

import android.content.Context
import com.lockguard.app.data.camera.CameraCaptureManager
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.repository.IntruderRepository
import com.lockguard.app.data.repository.IntruderRepositoryImpl
import com.lockguard.app.data.repository.SettingsRepository
import com.lockguard.app.data.repository.SettingsRepositoryImpl
import com.lockguard.app.data.security.BiometricHelper
import com.lockguard.app.data.security.CryptoManager
import com.lockguard.app.data.security.PinManager
import com.lockguard.app.service.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager {
        return CryptoManager(context)
    }

    @Provides
    @Singleton
    fun providePinManager(@ApplicationContext context: Context): PinManager {
        return PinManager(context)
    }

    @Provides
    @Singleton
    fun provideBiometricHelper(@ApplicationContext context: Context): BiometricHelper {
        return BiometricHelper(context)
    }

    @Provides
    @Singleton
    fun provideCameraCaptureManager(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): CameraCaptureManager {
        return CameraCaptureManager(context, cryptoManager)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideIntruderRepository(
        intruderDao: IntruderDao,
        cryptoManager: CryptoManager,
        cameraCaptureManager: CameraCaptureManager
    ): IntruderRepository {
        return IntruderRepositoryImpl(intruderDao, cryptoManager, cameraCaptureManager)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        pinManager: PinManager,
        biometricHelper: BiometricHelper,
        cameraCaptureManager: CameraCaptureManager,
        intruderDao: IntruderDao
    ): SettingsRepository {
        return SettingsRepositoryImpl(
            context,
            pinManager,
            biometricHelper,
            cameraCaptureManager,
            intruderDao
        )
    }
}
