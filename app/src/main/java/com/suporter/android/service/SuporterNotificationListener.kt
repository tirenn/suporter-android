package com.suporter.android.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.gson.Gson
import com.suporter.android.SuporterApp
import com.suporter.android.core.database.AppDatabase
import com.suporter.android.core.database.WebhookLogEntity
import com.suporter.android.core.network.ApiClient
import com.suporter.android.core.network.HmacHelper
import com.suporter.android.core.parser.AmountExtractor
import com.suporter.android.core.preferences.UserPreferences
import com.suporter.android.data.model.WebhookDonationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class SuporterNotificationListener : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val gson = Gson()

    private val db by lazy { AppDatabase.getInstance(this) }
    private val prefs by lazy { UserPreferences(this) }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener connected successfully.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        val combinedContent = "$title $text $bigText $subText".trim()
        if (combinedContent.isBlank()) return

        scope.launch {
            handleNotification(packageName, title, "$text $bigText $subText".trim(), combinedContent)
        }
    }

    private suspend fun handleNotification(
        packageName: String,
        title: String,
        bodyText: String,
        fullContent: String
    ) {
        // 1. Check if app is in monitored list
        val isMonitored = db.monitoredAppDao().isAppMonitored(packageName)
        if (!isMonitored) return

        val appName = getAppName(packageName)

        // 2. Check if text matches any active keyword
        val activeKeywords = db.keywordDao().getActiveKeywords()
        val lowerContent = fullContent.lowercase()
        val matchedKeyword = activeKeywords.firstOrNull { kw ->
            lowerContent.contains(kw.keyword.lowercase())
        }

        if (matchedKeyword == null) {
            Log.d(TAG, "Ignored notification from $appName: No keyword matched.")
            return
        }

        // 3. Extract transaction amount
        val extractedAmount = AmountExtractor.extractAmount(fullContent)
        if (extractedAmount == null || extractedAmount <= 0) {
            Log.d(TAG, "Matched keyword '${matchedKeyword.keyword}' but could not extract valid amount from: $fullContent")
            db.webhookLogDao().insertLog(
                WebhookLogEntity(
                    sourcePackage = packageName,
                    sourceAppName = appName,
                    notificationTitle = title,
                    notificationText = bodyText,
                    extractedAmount = 0,
                    requestUrl = "${prefs.getServerUrl()}/api/v1/webhooks/donation",
                    requestHeaders = "-",
                    requestPayload = "-",
                    responseCode = 0,
                    responseBody = "Keyword matched ('${matchedKeyword.keyword}') but no numerical amount could be extracted",
                    status = "IGNORED"
                )
            )
            return
        }

        Log.d(TAG, "Extracted Rp $extractedAmount from $appName (Matched keyword: ${matchedKeyword.keyword})")

        // 4. Send Webhook to Suporter Backend
        sendWebhook(packageName, appName, title, bodyText, extractedAmount)
    }

    private suspend fun sendWebhook(
        packageName: String,
        appName: String,
        title: String,
        bodyText: String,
        amount: Long
    ) {
        val serverUrl = prefs.getServerUrl()
        val webhookKey = prefs.getWebhookKey()
        val webhookSecret = prefs.getWebhookSecret() ?: ""

        if (webhookKey.isNullOrBlank()) {
            Log.w(TAG, "Cannot dispatch webhook: User is not logged in or Webhook Key is missing.")
            return
        }

        if (!prefs.hasActiveProject()) {
            Log.d(TAG, "User has no active overlay project.")
            db.webhookLogDao().insertLog(
                WebhookLogEntity(
                    sourcePackage = packageName,
                    sourceAppName = appName,
                    notificationTitle = title,
                    notificationText = bodyText,
                    extractedAmount = amount,
                    requestUrl = "${serverUrl.trimEnd('/')}/api/v1/webhooks/donation",
                    requestHeaders = "-",
                    requestPayload = "-",
                    responseCode = 0,
                    responseBody = "Pengiriman dibatalkan: Akun streamer belum memiliki project OBS overlay aktif",
                    status = "NO_PROJECT"
                )
            )
            return
        }

        if (!prefs.isWebhookForwardingEnabled()) {
            Log.d(TAG, "Webhook forwarding is disabled by user.")
            db.webhookLogDao().insertLog(
                WebhookLogEntity(
                    sourcePackage = packageName,
                    sourceAppName = appName,
                    notificationTitle = title,
                    notificationText = bodyText,
                    extractedAmount = amount,
                    requestUrl = "${serverUrl.trimEnd('/')}/api/v1/webhooks/donation",
                    requestHeaders = "-",
                    requestPayload = "-",
                    responseCode = 0,
                    responseBody = "Pengiriman Webhook sedang dinonaktifkan oleh pengguna (Paused)",
                    status = "PAUSED"
                )
            )
            return
        }

        val requestObj = WebhookDonationRequest(amount = amount)
        val rawJsonBody = gson.toJson(requestObj)
        val timestamp = System.currentTimeMillis() / 1000
        val signature = HmacHelper.generateSignature(webhookSecret, timestamp, rawJsonBody)

        val requestUrl = "${serverUrl.trimEnd('/')}/api/v1/webhooks/donation"
        val requestHeaders = "X-Suporter-Key: $webhookKey\nX-Suporter-Timestamp: $timestamp\nX-Suporter-Signature: $signature"

        try {
            val apiService = ApiClient.getService(serverUrl)
            val requestBody = rawJsonBody.toRequestBody("application/json".toMediaType())

            val response = apiService.verifyWebhookRaw(
                webhookKey = webhookKey,
                timestamp = timestamp.toString(),
                signature = signature,
                rawBody = requestBody
            )

            val code = response.code()
            val respBodyString = response.body()?.string() ?: response.errorBody()?.string() ?: ""

            val status = if (response.isSuccessful) "SUCCESS" else "FAILED"

            db.webhookLogDao().insertLog(
                WebhookLogEntity(
                    sourcePackage = packageName,
                    sourceAppName = appName,
                    notificationTitle = title,
                    notificationText = bodyText,
                    extractedAmount = amount,
                    requestUrl = requestUrl,
                    requestHeaders = requestHeaders,
                    requestPayload = rawJsonBody,
                    responseCode = code,
                    responseBody = respBodyString,
                    status = status
                )
            )

            if (response.isSuccessful) {
                com.suporter.android.core.analytics.AnalyticsHelper(applicationContext).logWebhookSent(appName, amount, true)
                Log.d(TAG, "Webhook dispatched successfully for Rp $amount from $appName!")
            } else {
                com.suporter.android.core.analytics.AnalyticsHelper(applicationContext).logWebhookSent(appName, amount, false)
                Log.e(TAG, "Webhook failed with HTTP $code: $respBodyString")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Network error dispatching webhook", e)
            db.webhookLogDao().insertLog(
                WebhookLogEntity(
                    sourcePackage = packageName,
                    sourceAppName = appName,
                    notificationTitle = title,
                    notificationText = bodyText,
                    extractedAmount = amount,
                    requestUrl = requestUrl,
                    requestHeaders = requestHeaders,
                    requestPayload = rawJsonBody,
                    responseCode = 0,
                    responseBody = "Network error: ${e.localizedMessage ?: e.message}",
                    status = "FAILED"
                )
            )
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = applicationContext.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "SuporterNotifListener"
    }
}
