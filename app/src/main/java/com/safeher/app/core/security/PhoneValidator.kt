package com.safeher.app.core.security

import java.util.regex.Pattern

object PhoneValidator {

    // E.164 and international/national standard formats validation
    private val PHONE_PATTERN = Pattern.compile(
        "^(\\+?\\d{1,4}[-\\s]?)?\\(?\\d{2,4}\\)?[-\\s]?\\d{3,4}[-\\s]?\\d{3,4}$"
    )

    fun isValidPhoneNumber(phone: String): Boolean {
        val trimmed = phone.trim()
        val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")
        if (digitsOnly.length < 8 || digitsOnly.length > 15) return false
        return PHONE_PATTERN.matcher(trimmed).matches()
    }

    fun sanitizePhoneNumber(phone: String): String {
        return phone.trim().replace(Regex("[^0-9+]"), "")
    }
}

object SecurityUtils {
    fun generateSosSmsBody(
        userName: String,
        latitude: Double?,
        longitude: Double?,
        address: String? = null
    ): String {
        val name = if (userName.isNotBlank()) userName else "SafeHer User"
        val locationPart = if (latitude != null && longitude != null) {
            val addr = if (!address.isNullOrBlank()) " ($address)" else ""
            "\nLocation: https://maps.google.com/?q=$latitude,$longitude$addr"
        } else {
            "\nLocation: Unavailable at trigger time."
        }
        return "[EMERGENCY ALERT from $name]\nI am in danger and need immediate help!$locationPart\nSent via SafeHer App."
    }

    fun generateTestSmsBody(userName: String): String {
        val name = if (userName.isNotBlank()) userName else "SafeHer User"
        return "[SafeHer Test Alert]\n$name has verified you as their trusted emergency contact. No action is required."
    }
}
