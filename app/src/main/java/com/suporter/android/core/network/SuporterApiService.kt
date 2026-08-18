package com.suporter.android.core.network

import com.suporter.android.data.model.AuthResponse
import com.suporter.android.data.model.CreateDonationRequest
import com.suporter.android.data.model.DonationResponse
import com.suporter.android.data.model.MobileLoginRequest
import com.suporter.android.data.model.WebhookDonationRequest
import com.suporter.android.data.model.WebhookDonationResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SuporterApiService {

    @POST("api/v1/auth/mobile-login")
    suspend fun mobileLogin(
        @Body request: MobileLoginRequest
    ): Response<AuthResponse>

    @POST("api/v1/donations")
    suspend fun createDonation(
        @Header("X-Is-Test") isTest: String = "true",
        @Body request: CreateDonationRequest
    ): Response<DonationResponse>

    @POST("api/v1/webhooks/donation")
    suspend fun verifyWebhook(
        @Header("X-Suporter-Key") webhookKey: String,
        @Header("X-Suporter-Timestamp") timestamp: String,
        @Header("X-Suporter-Signature") signature: String,
        @Body request: WebhookDonationRequest
    ): Response<WebhookDonationResponse>

    @POST("api/v1/webhooks/donation")
    suspend fun verifyWebhookRaw(
        @Header("X-Suporter-Key") webhookKey: String,
        @Header("X-Suporter-Timestamp") timestamp: String,
        @Header("X-Suporter-Signature") signature: String,
        @Body rawBody: RequestBody
    ): Response<ResponseBody>

    @retrofit2.http.GET("api/v1/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<com.suporter.android.data.model.User>

    @retrofit2.http.GET("api/v1/projects")
    suspend fun getUserProjects(
        @Header("Authorization") authorization: String
    ): Response<com.suporter.android.data.model.ProjectsResponse>
}
