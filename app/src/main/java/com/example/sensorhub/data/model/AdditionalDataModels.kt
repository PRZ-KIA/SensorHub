package com.example.sensorhub.data.model

/**
 * Data models for Light Sensor
 */
data class LightData(
    val illuminance: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data models for GPS/Location
 */
data class GpsData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val accuracy: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data models for Proximity Sensor
 */
data class ProximityData(
    val distance: Float = 0f,
    val isNear: Boolean = false,
    val maxRange: Float = 5f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data models for Barometer
 */
data class BarometerData(
    val pressure: Float = 0f,
    val altitude: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Generic sensor data interface
 */
interface SensorData {
    val timestamp: Long
}

/**
 * Sensor type enumeration
 */
enum class SensorType(val displayName: String) {
    ACCELEROMETER("Accelerometer"),
    GYROSCOPE("Gyroscope"),
    MAGNETOMETER("Magnetometer"),
    LIGHT("Light Sensor"),
    GPS("GPS / Location"),
    PROXIMITY("Proximity"),
    BAROMETER("Barometer"),
    UNKNOWN("Unknown");
    
    companion object {
        fun fromString(type: String): SensorType {
            return values().find { 
                it.name.equals(type, ignoreCase = true) 
            } ?: UNKNOWN
        }
    }
}

/**
 * Sensor status
 */
enum class SensorStatus {
    AVAILABLE,
    UNAVAILABLE,
    PERMISSION_REQUIRED,
    DISABLED,
    ERROR
}

/**
 * Sensor configuration
 */
data class SensorConfig(
    val sensorType: SensorType,
    val samplingRate: SamplingRate = SamplingRate.NORMAL,
    val isEnabled: Boolean = true,
    val autoSave: Boolean = false
)

/**
 * Sampling rate options
 */
enum class SamplingRate(val delayMicroseconds: Int, val displayName: String) {
    FASTEST(0, "Fastest (~200Hz)"),
    FAST(10000, "Fast (~100Hz)"),
    NORMAL(200000, "Normal (~5Hz)"),
    SLOW(1000000, "Slow (~1Hz)");
    
    companion object {
        fun fromDelay(delay: Int): SamplingRate {
            return values().minByOrNull { 
                kotlin.math.abs(it.delayMicroseconds - delay) 
            } ?: NORMAL
        }
    }
}
