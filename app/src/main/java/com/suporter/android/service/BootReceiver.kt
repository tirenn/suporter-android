package com.suporter.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suporter.android.core.preferences.UserPreferences

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val prefs = UserPreferences(context)
            if (prefs.isLoggedIn() && prefs.isKeepAliveEnabled()) {
                KeepAliveForegroundService.start(context)
            }
        }
    }
}
