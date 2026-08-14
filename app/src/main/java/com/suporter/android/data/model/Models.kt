package com.suporter.android.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    val username: String,
    val name: String,
    val role: String,
    @SerializedName("webhook_key") val webhookKey: String?,
    @SerializedName("webhook_secret") val webhookSecret: String?,
    @SerializedName("qris_url") val qrisUrl: String?,
    @SerializedName("is_active") val isActive: Boolean
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    val user: User
)

data class MobileLoginRequest(
    val username: String,
    val password: String
)

data class CreateDonationRequest(
    @SerializedName("streamer_username") val streamerUsername: String,
    @SerializedName("sender_name") val senderName: String,
    val amount: Long,
    val message: String,
    @SerializedName("recaptcha_token") val recaptchaToken: String = "mobile_bypass"
)

data class DonationResponse(
    val id: Long,
    @SerializedName("streamer_id") val streamerId: Long,
    @SerializedName("sender_name") val senderName: String,
    val amount: Long,
    @SerializedName("unique_code") val uniqueCode: Int,
    @SerializedName("total_amount") val totalAmount: Long,
    val message: String?,
    val status: String,
    @SerializedName("is_test") val isTest: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class WebhookDonationRequest(
    val amount: Long
)

data class WebhookDonationResponse(
    val status: String,
    val message: String,
    val donation: DonationResponse?
)

data class DashboardStats(
    val totalCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val isListenerActive: Boolean,
    val isBatteryOptimized: Boolean,
    val isKeepAliveRunning: Boolean
)
