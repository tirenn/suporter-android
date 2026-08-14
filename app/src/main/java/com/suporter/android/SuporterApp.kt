package com.suporter.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.suporter.android.core.database.AppDatabase
import com.suporter.android.core.preferences.UserPreferences
import com.suporter.android.service.KeepAliveForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SuporterApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val preferences by lazy { UserPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Eagerly initialize Database & default keywords
        CoroutineScope(Dispatchers.IO).launch {
            database.keywordDao().countKeywords()
        }

        // Auto-start keep-alive foreground service if user is logged in
        if (preferences.isLoggedIn() && preferences.isKeepAliveEnabled()) {
            KeepAliveForegroundService.start(this)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundChannel = NotificationChannel(
                KeepAliveForegroundService.CHANNEL_ID,
                "Suporter Keep-Alive Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status background listener Suporter"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                "suporter_donation_alerts",
                "Notifikasi Donasi Masuk",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pemberitahuan saat donasi berhasil dideteksi dan dikirim"
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(foregroundChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }
}
