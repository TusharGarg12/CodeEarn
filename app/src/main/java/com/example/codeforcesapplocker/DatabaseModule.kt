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
        )
            .fallbackToDestructiveMigration() // Useful for dev; wipes data if you change table structure
            .build()
    }

    @Provides
    @Singleton // It is good practice to make the DAO a singleton too since the DB is one
    fun provideAppDao(db: AppDatabase): AppDao {
        return db.appDao()
    }
}