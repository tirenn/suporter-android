package com.suporter.android.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keywords")
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val isSuggested: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "webhook_logs")
data class WebhookLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sourcePackage: String,
    val sourceAppName: String,
    val notificationTitle: String,
    val notificationText: String,
    val extractedAmount: Long,
    val requestUrl: String,
    val requestHeaders: String,
    val requestPayload: String,
    val responseCode: Int,
    val responseBody: String,
    val status: String // "SUCCESS", "FAILED", "IGNORED"
)
