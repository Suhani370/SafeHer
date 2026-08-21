package com.safeher.app

import com.safeher.app.core.security.PhoneValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneValidatorTest {

    @Test
    fun validPhoneNumbers_returnTrue() {
        assertTrue(PhoneValidator.isValidPhoneNumber("+919876543210"))
        assertTrue(PhoneValidator.isValidPhoneNumber("9876543210"))
        assertTrue(PhoneValidator.isValidPhoneNumber("+14155552671"))
        assertTrue(PhoneValidator.isValidPhoneNumber("+442071838750"))
        assertTrue(PhoneValidator.isValidPhoneNumber("022-24301234"))
    }

    @Test
    fun invalidPhoneNumbers_returnFalse() {
        assertFalse(PhoneValidator.isValidPhoneNumber(""))
        assertFalse(PhoneValidator.isValidPhoneNumber("123"))
        assertFalse(PhoneValidator.isValidPhoneNumber("abcdefghij"))
        assertFalse(PhoneValidator.isValidPhoneNumber("++919876543210"))
    }

    @Test
    fun sanitizePhoneNumber_stripsUnwantedCharacters() {
        assertEquals("+919876543210", PhoneValidator.sanitizePhoneNumber("+91 98765-43210"))
        assertEquals("9876543210", PhoneValidator.sanitizePhoneNumber("(987) 654-3210"))
    }
}
