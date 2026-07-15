package com.logger.logger_sdk.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface LogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        log: LogEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        logs: List<LogEntity>
    )

    @Query(
        """
        SELECT *
        FROM offline_logs
        ORDER BY id ASC
        LIMIT :limit
        """
    )
    suspend fun getLogs(
        limit: Int
    ): List<LogEntity>

    @Query("DELETE FROM offline_logs WHERE id IN (:ids)")
    suspend fun deleteByIds(
        ids: List<Long>
    )

    @Query("DELETE FROM offline_logs")
    suspend fun clear()
}