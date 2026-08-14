package com.suporter.android.data.repository

import com.suporter.android.core.database.WebhookLogDao
import com.suporter.android.core.database.WebhookLogEntity
import kotlinx.coroutines.flow.Flow

class LogRepository(private val webhookLogDao: WebhookLogDao) {

    val allLogs: Flow<List<WebhookLogEntity>> = webhookLogDao.getAllLogs()
    val totalCount: Flow<Int> = webhookLogDao.countTotalLogs()
    val successCount: Flow<Int> = webhookLogDao.countSuccessLogs()
    val failedCount: Flow<Int> = webhookLogDao.countFailedLogs()

    fun getLogsByStatus(status: String): Flow<List<WebhookLogEntity>> {
        return if (status == "ALL") webhookLogDao.getAllLogs()
        else webhookLogDao.getLogsByStatus(status)
    }

    suspend fun clearLogs() {
        webhookLogDao.clearAllLogs()
    }
}
