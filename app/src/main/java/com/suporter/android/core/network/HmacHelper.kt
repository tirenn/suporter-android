package com.suporter.android.core.network

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HmacHelper {

    fun generateSignature(secret: String, timestamp: Long, rawBodyJson: String): String {
        val payloadToSign = "$timestamp.$rawBodyJson"
        return calculateHmacSha256(secret, payloadToSign)
    }

    fun calculateHmacSha256(secret: String, data: String): String {
        return try {
            val algorithm = "HmacSHA256"
            val keySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), algorithm)
            val mac = Mac.getInstance(algorithm)
            mac.init(keySpec)
            val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            bytesToHex(hmacBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }
}
