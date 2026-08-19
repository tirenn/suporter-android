package com.suporter.android.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logScreen(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logLogin(username: String) {
        val bundle = Bundle().apply {
            putString("username", username)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logWebhookSent(packageName: String, amount: Long, isSuccess: Boolean) {
        val bundle = Bundle().apply {
            putString("package_name", packageName)
            putLong("amount", amount)
            putBoolean("success", isSuccess)
        }
        firebaseAnalytics.logEvent("webhook_dispatched", bundle)
    }

    fun logTestWebhook(amount: Long, isSuccess: Boolean) {
        val bundle = Bundle().apply {
            putLong("amount", amount)
            putBoolean("success", isSuccess)
        }
        firebaseAnalytics.logEvent("playground_webhook_test", bundle)
    }
}
