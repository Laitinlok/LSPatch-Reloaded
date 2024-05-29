package org.lsposed.lspatch.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.lsposed.lspatch.database.LSPDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideLSPDatabase(
        @ApplicationContext context: Context
    ): LSPDatabase {
        return Room.databaseBuilder(
            context, LSPDatabase::class.java, "modules_config.db"
        ).build()
    }
}