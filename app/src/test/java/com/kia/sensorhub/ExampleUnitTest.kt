package com.kia.sensorhub

import com.kia.sensorhub.utils.ValidationUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun isRecent_returnsFalseForFutureTimestamp() {
        val now = System.currentTimeMillis()

        val result = ValidationUtils.isRecent(
            timestamp = now + 1_000,
            withinMs = 5_000
        )

        assertFalse(result)
    }

    @Test
    fun isRecent_returnsTrueForTimestampInWindow() {
        val now = System.currentTimeMillis()

        val result = ValidationUtils.isRecent(
            timestamp = now - 1_000,
            withinMs = 5_000
        )

        assertTrue(result)
    }

    @Test
    fun isRecent_returnsFalseForTimestampOutsideWindow() {
        val now = System.currentTimeMillis()

        val result = ValidationUtils.isRecent(
            timestamp = now - 10_000,
            withinMs = 5_000
        )

        assertFalse(result)
    }
}
