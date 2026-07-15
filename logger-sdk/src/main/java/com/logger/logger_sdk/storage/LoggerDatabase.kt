package com.logger.logger_sdk.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LogEntity::class
    ],
    version = 1,
    exportSchema = true
)
internal abstract class LoggerDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao

    companion object {
        private const val DATABASE_NAME = "logger_database"
        @Volatile
        private var INSTANCE: LoggerDatabase? = null

        fun getInstance(context: Context): LoggerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LoggerDatabase::class.java,
                    DATABASE_NAME,
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}