package com.logger.logger_sdk.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "offline_logs")
internal data class LogEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    /**
     * Serialize edilmiş log.
     */
    val payload: String,

    /**
     * Log oluşturulma zamanı.
     */
    val createdAt: Long = System.currentTimeMillis()
)