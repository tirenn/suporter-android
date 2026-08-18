package com.suporter.android.data.repository

import com.suporter.android.core.network.ApiClient
import com.suporter.android.core.preferences.UserPreferences
import com.suporter.android.data.model.AuthResponse
import com.suporter.android.data.model.MobileLoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val preferences: UserPreferences) {

    suspend fun login(username: String, password: String, serverUrl: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.getService(serverUrl)
                val response = api.mobileLogin(MobileLoginRequest(username = username, password = password))
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    preferences.saveUserSession(authBody.accessToken, authBody.user, serverUrl)
                    Result.success(authBody)
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = com.suporter.android.core.network.ErrorParser.parse(rawError, "Login gagal (${response.code()})")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server backend"))
            }
        }
    }

    suspend fun checkProfile(serverUrl: String, token: String): Result<com.suporter.android.data.model.User> {
        return withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.getService(serverUrl)
                val response = api.getProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    if (!user.isActive) {
                        Result.failure(Exception("SESSION_EXPIRED: Akun Anda dinonaktifkan oleh admin"))
                    } else {
                        // Keep cached user details synchronized
                        preferences.saveUserSession(token, user, serverUrl)
                        Result.success(user)
                    }
                } else if (response.code() == 401 || response.code() == 403 || response.code() == 404) {
                    Result.failure(Exception("SESSION_EXPIRED"))
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = com.suporter.android.core.network.ErrorParser.parse(rawError, "Gagal memverifikasi akun (${response.code()})")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server backend"))
            }
        }
    }

    suspend fun checkUserProjects(serverUrl: String, token: String): Result<List<com.suporter.android.data.model.ProjectDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.getService(serverUrl)
                val response = api.getUserProjects("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val projects = response.body()!!.projects
                    preferences.setHasActiveProject(projects.isNotEmpty())
                    Result.success(projects)
                } else if (response.code() == 401 || response.code() == 403 || response.code() == 404) {
                    Result.failure(Exception("SESSION_EXPIRED"))
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = com.suporter.android.core.network.ErrorParser.parse(rawError, "Gagal mengambil data project (${response.code()})")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server backend"))
            }
        }
    }

    fun logout() {
        preferences.clearSession()
    }
}
