package com.kia.sensorhub

import com.kia.sensorhub.utils.ValidationUtils
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @After
    fun tearDown() {
        ValidationUtils.resetCurrentTimeProvider()
    }

    @Test
    fun isRecent_returnsFalseForFutureTimestamp() {
        val fixedNow = 1_000_000L
        ValidationUtils.setCurrentTimeProvider { fixedNow }

        val result = ValidationUtils.isRecent(
            timestamp = fixedNow + 1_000,
            withinMs = 5_000
        )

        assertFalse(result)
    }

    @Test
    fun isRecent_returnsTrueForTimestampInWindow() {
        val fixedNow = 1_000_000L
        ValidationUtils.setCurrentTimeProvider { fixedNow }

        val result = ValidationUtils.isRecent(
            timestamp = fixedNow - 1_000,
            withinMs = 5_000
        )

        assertTrue(result)
    }

    @Test
    fun isRecent_returnsFalseForTimestampOutsideWindow() {
        val fixedNow = 1_000_000L
        ValidationUtils.setCurrentTimeProvider { fixedNow }

        val result = ValidationUtils.isRecent(
            timestamp = fixedNow - 10_000,
            withinMs = 5_000
        )

        assertFalse(result)
    }

    @Test
    fun isRecent_returnsTrueWhenDeltaEqualsZero() {
        val fixedNow = 1_000_000L
        ValidationUtils.setCurrentTimeProvider { fixedNow }

        assertTrue(
            ValidationUtils.isRecent(
                timestamp = fixedNow,
                withinMs = 5_000
            )
        )
    }

    @Test
    fun isRecent_returnsTrueWhenDeltaEqualsWindowBoundary() {
        val fixedNow = 1_000_000L
        ValidationUtils.setCurrentTimeProvider { fixedNow }

        assertTrue(
            ValidationUtils.isRecent(
                timestamp = fixedNow - 5_000,
                withinMs = 5_000
            )
        )
    }

    @Test
    fun isRecent_returnsFalseForNegativeWindow() {
        val fixedNow = 1_000_000L
        ValidationUtils.setCurrentTimeProvider { fixedNow }

        assertFalse(
            ValidationUtils.isRecent(
                timestamp = fixedNow - 1_000,
                withinMs = -1
            )
        )
    }
}
