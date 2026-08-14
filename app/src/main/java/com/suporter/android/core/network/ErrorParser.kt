package com.suporter.android.core.network

import org.json.JSONObject

object ErrorParser {

    /**
     * Parses raw error body string (JSON or plain text) and extracts the human-readable error message.
     */
    fun parse(raw: String?, defaultMessage: String = "Terjadi kesalahan"): String {
        if (raw.isNullOrBlank()) return defaultMessage
        return try {
            val trimmed = raw.trim()
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val json = JSONObject(trimmed)
                when {
                    json.has("error") -> json.getString("error")
                    json.has("message") -> json.getString("message")
                    else -> raw
                }
            } else {
                raw
            }
        } catch (e: Exception) {
            raw
        }
    }

    /**
     * Parses rate-limit retry_after seconds from backend JSON error response.
     */
    fun parseRetryAfter(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        return try {
            val trimmed = raw.trim()
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val json = JSONObject(trimmed)
                if (json.has("retry_after")) {
                    json.getInt("retry_after")
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
