package com.suporter.android.core.parser

import java.util.regex.Pattern

object AmountExtractor {

    // Regex patterns for various Indonesian banking and e-wallet notification formats
    private val patterns = listOf(
        // e.g., "Rp 50.000", "Rp. 50.000", "Rp50000", "Rp 50,000"
        Pattern.compile("""(?:Rp\.?|IDR)\s*([0-9]{1,3}(?:[.,][0-9]{3})*|\b[0-9]{4,8}\b)""", Pattern.CASE_INSENSITIVE),
        // e.g., "sebesar Rp 50.000", "sebesar 50000"
        Pattern.compile("""(?:sebesar|nominal|jumlah|total)\s*(?:Rp\.?|IDR)?\s*([0-9]{1,3}(?:[.,][0-9]{3})*|\b[0-9]{4,8}\b)""", Pattern.CASE_INSENSITIVE),
        // e.g., "+ Rp 50.000", "+Rp50.000", "+50.000"
        Pattern.compile("""\+\s*(?:Rp\.?|IDR)?\s*([0-9]{1,3}(?:[.,][0-9]{3})*|\b[0-9]{4,8}\b)""", Pattern.CASE_INSENSITIVE),
        // e.g., "Transfer masuk 50.000"
        Pattern.compile("""(?:masuk|diterima|berhasil)\s*(?:sebesar)?\s*(?:Rp\.?|IDR)?\s*([0-9]{1,3}(?:[.,][0-9]{3})*|\b[0-9]{4,8}\b)""", Pattern.CASE_INSENSITIVE)
    )

    /**
     * Extracts numerical amount from notification title or content text.
     * Returns the parsed amount in Rupiah as Long, or null if no valid amount found.
     */
    fun extractAmount(text: String): Long? {
        if (text.isBlank()) return null

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val rawGroup = matcher.group(1) ?: continue
                val cleanAmount = parseNumberString(rawGroup)
                if (cleanAmount != null && cleanAmount >= 1000) {
                    return cleanAmount
                }
            }
        }

        // Fallback: search for stand-alone formatted number patterns like "50.142" or "100.000"
        val generalNumberPattern = Pattern.compile("""\b([0-9]{1,3}(?:[.,][0-9]{3})+)\b""")
        val generalMatcher = generalNumberPattern.matcher(text)
        if (generalMatcher.find()) {
            val rawGroup = generalMatcher.group(1) ?: return null
            val cleanAmount = parseNumberString(rawGroup)
            if (cleanAmount != null && cleanAmount >= 1000) {
                return cleanAmount
            }
        }

        return null
    }

    private fun parseNumberString(raw: String): Long? {
        return try {
            // Remove all dots, commas, and spaces to get pure digits
            val digitsOnly = raw.replace(".", "").replace(",", "").replace(" ", "").trim()
            digitsOnly.toLong()
        } catch (e: Exception) {
            null
        }
    }
}
