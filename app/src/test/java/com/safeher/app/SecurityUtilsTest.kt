package com.safeher.app

import com.safeher.app.core.security.SecurityUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun generateSosSmsBody_containsLocationAndGoogleMapsLink() {
        val sms = SecurityUtils.generateSosSmsBody(
            userName = "Priya Sharma",
            latitude = 28.6139,
            longitude = 77.2090,
            address = "Connaught Place, New Delhi"
        )

        assertTrue(sms.contains("EMERGENCY ALERT from Priya Sharma"))
        assertTrue(sms.contains("https://maps.google.com/?q=28.6139,77.209"))
        assertTrue(sms.contains("Connaught Place"))
        assertTrue(sms.contains("SafeHer App"))
    }

    @Test
    fun generateSosSmsBody_handlesNullLocationGracefully() {
        val sms = SecurityUtils.generateSosSmsBody(
            userName = "Priya Sharma",
            latitude = null,
            longitude = null,
            address = null
        )

        assertTrue(sms.contains("EMERGENCY ALERT from Priya Sharma"))
        assertTrue(sms.contains("Unavailable at trigger time"))
    }

    @Test
    fun generateTestSmsBody_containsTestDisclaimer() {
        val testSms = SecurityUtils.generateTestSmsBody("Priya Sharma")
        assertTrue(testSms.contains("[SafeHer Test Alert]"))
        assertTrue(testSms.contains("No action is required"))
    }
}
