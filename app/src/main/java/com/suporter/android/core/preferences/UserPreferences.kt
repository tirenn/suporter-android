package com.suporter.android.core.preferences

import android.content.Context
import android.content.SharedPreferences
import com.suporter.android.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("suporter_prefs", Context.MODE_PRIVATE)

    private val _isLoggedInFlow = MutableStateFlow(isLoggedIn())
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank() && !getWebhookKey().isNullOrBlank()
    }

    fun saveUserSession(token: String, user: User, serverUrl: String) {
        prefs.edit()
            .putString("access_token", token)
            .putLong("user_id", user.id)
            .putString("username", user.username)
            .putString("name", user.name)
            .putString("webhook_key", user.webhookKey ?: "")
            .putString("webhook_secret", user.webhookSecret ?: "")
            .putString("qris_url", user.qrisUrl ?: "")
            .putBoolean("is_active", user.isActive)
            .putString("server_url", serverUrl)
            .apply()
        _isLoggedInFlow.value = true
    }

    fun clearSession() {
        prefs.edit()
            .remove("access_token")
            .remove("user_id")
            .remove("username")
            .remove("name")
            .remove("webhook_key")
            .remove("webhook_secret")
            .remove("qris_url")
            .remove("is_active")
            .apply()
        _isLoggedInFlow.value = false
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getUsername(): String? = prefs.getString("username", null)
    fun getName(): String? = prefs.getString("name", null)
    fun getWebhookKey(): String? = prefs.getString("webhook_key", null)
    fun getWebhookSecret(): String? = prefs.getString("webhook_secret", null)
    fun getQrisUrl(): String? = prefs.getString("qris_url", null)
    fun getIsActive(): Boolean = prefs.getBoolean("is_active", false)

    fun getServerUrl(): String = prefs.getString("server_url", "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080"
    fun setServerUrl(url: String) {
        prefs.edit().putString("server_url", url).apply()
    }

    fun isKeepAliveEnabled(): Boolean = prefs.getBoolean("keep_alive_enabled", true)
    fun setKeepAliveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("keep_alive_enabled", enabled).apply()
    }

    fun isWebhookForwardingEnabled(): Boolean = prefs.getBoolean("webhook_forwarding_enabled", true)
    fun setWebhookForwardingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("webhook_forwarding_enabled", enabled).apply()
    }

    fun hasActiveProject(): Boolean = prefs.getBoolean("has_active_project", true)
    fun setHasActiveProject(hasProject: Boolean) {
        prefs.edit().putBoolean("has_active_project", hasProject).apply()
    }
}
