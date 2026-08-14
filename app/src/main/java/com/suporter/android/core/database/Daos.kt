package com.suporter.android.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordDao {
    @Query("SELECT * FROM keywords ORDER BY isDefault DESC, keyword ASC")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    @Query("SELECT * FROM keywords WHERE isEnabled = 1")
    suspend fun getActiveKeywords(): List<KeywordEntity>

    @Query("SELECT COUNT(*) FROM keywords")
    suspend fun countKeywords(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(keywords: List<KeywordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyword: KeywordEntity): Long

    @Update
    suspend fun update(keyword: KeywordEntity)

    @Delete
    suspend fun delete(keyword: KeywordEntity)
}

@Dao
interface MonitoredAppDao {
    @Query("SELECT * FROM monitored_apps ORDER BY isEnabled DESC, appName ASC")
    fun getAllMonitoredApps(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT * FROM monitored_apps WHERE isEnabled = 1")
    suspend fun getActiveMonitoredApps(): List<MonitoredAppEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM monitored_apps WHERE packageName = :pkg AND isEnabled = 1)")
    suspend fun isAppMonitored(pkg: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: MonitoredAppEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<MonitoredAppEntity>)

    @Delete
    suspend fun delete(app: MonitoredAppEntity)
}

@Dao
interface WebhookLogDao {
    @Query("SELECT * FROM webhook_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WebhookLogEntity>>

    @Query("SELECT * FROM webhook_logs WHERE status = :status ORDER BY timestamp DESC")
    fun getLogsByStatus(status: String): Flow<List<WebhookLogEntity>>

    @Query("SELECT COUNT(*) FROM webhook_logs")
    fun countTotalLogs(): Flow<Int>

    @Query("SELECT COUNT(*) FROM webhook_logs WHERE status = 'SUCCESS'")
    fun countSuccessLogs(): Flow<Int>

    @Query("SELECT COUNT(*) FROM webhook_logs WHERE status = 'FAILED'")
    fun countFailedLogs(): Flow<Int>

    @Insert
    suspend fun insertLog(log: WebhookLogEntity): Long

    @Query("DELETE FROM webhook_logs")
    suspend fun clearAllLogs()
}
