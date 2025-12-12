package com.example.codeforcesapplocker

import android.content.Context
import androidx.room.Room
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "codeearn_db"
        ).fallbackToDestructiveMigration() // Wipes DB if you change schema (good for dev)
            .build()
    }

    @Provides
    fun provideAppDao(db: AppDatabase): AppDao {
        return db.appDao()
    }
}