package com.logger.logger_sdk.storage

internal class RoomOfflineRepository<T>(
    private val dao: LogDao,
    private val serializer: LogSerializer<T>,
) : OfflineLogRepository<T> {

    override suspend fun insert(log: T) {
        dao.insert(
            LogEntity(
                payload = serializer.serialize(log)
            )
        )
    }

    override suspend fun insertAll(logs: List<T>) {
        dao.insertAll(
            logs.map {
                LogEntity(
                    payload = serializer.serialize(it)
                )
            }
        )
    }

    override suspend fun getLogs(
        limit: Int
    ): List<OfflineLog<T>> {
        return dao.getLogs(limit).map { entity ->
            OfflineLog(
                id = entity.id,
                uuid = entity.uuid,
                value = serializer.deserialize(entity.payload),
                createdAt = entity.createdAt,
            )
        }
    }

    override suspend fun delete(
        logs: List<Long>
    ) {
        dao.deleteByIds(logs)
    }
}