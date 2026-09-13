package com.lockguard.app.di

import android.content.Context
import androidx.room.Room
import com.lockguard.app.data.database.IntruderDao
import com.lockguard.app.data.database.LockGuardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLockGuardDatabase(
        @ApplicationContext context: Context
    ): LockGuardDatabase {
        return Room.databaseBuilder(
            context,
            LockGuardDatabase::class.java,
            "lockguard_secure.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideIntruderDao(database: LockGuardDatabase): IntruderDao {
        return database.intruderDao()
    }
}
