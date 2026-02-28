package com.kia.sensorhub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Base sealed class for all sensor data types
 */
sealed class SensorData {
    abstract val timestamp: Long
    abstract val sensorType: SensorType
}

/**
 * Enum representing all supported sensor types
 */
enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    LIGHT,
    GPS,
    PROXIMITY,
    BAROMETER,
    UNKNOWN
}

/**
 * Accelerometer data (m/s²)
 */
data class AccelerometerData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.ACCELEROMETER,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f
) : SensorData()

/**
 * Gyroscope data (rad/s)
 */
data class GyroscopeData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.GYROSCOPE,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val rotationRate: Float = 0f
) : SensorData()

/**
 * Magnetometer data (µT)
 */
data class MagnetometerData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.MAGNETOMETER,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val azimuth: Float = 0f // Direction in degrees
) : SensorData()

/**
 * Light sensor data (lux)
 */
data class LightData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.LIGHT,
    val illuminance: Float = 0f
) : SensorData()

/**
 * GPS location data
 */
data class GpsData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.GPS,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val accuracy: Float = 0f,
    val bearing: Float = 0f
) : SensorData()

/**
 * Proximity sensor data (cm)
 */
data class ProximityData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.PROXIMITY,
    val distance: Float = 0f,
    val isNear: Boolean = false
) : SensorData()

/**
 * Barometer data (hPa)
 */
data class BarometerData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.BAROMETER,
    val pressure: Float = 0f,
    val altitude: Float = 0f // Calculated from pressure
) : SensorData()

/**
 * Room entity for storing sensor readings in database
 */
@Entity(tableName = "sensor_readings")
data class SensorReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sensorType: String,
    val valueX: Float = 0f,
    val valueY: Float = 0f,
    val valueZ: Float = 0f,
    val valueExtra: Float = 0f, // For magnitude, azimuth, etc.
    val accuracy: Float = 0f
)

/**
 * Extension function to convert SensorData to SensorReading
 */
fun SensorData.toSensorReading(): SensorReading {
    return when (this) {
        is AccelerometerData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = x,
            valueY = y,
            valueZ = z,
            valueExtra = magnitude
        )
        is GyroscopeData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = x,
            valueY = y,
            valueZ = z,
            valueExtra = rotationRate
        )
        is MagnetometerData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = x,
            valueY = y,
            valueZ = z,
            valueExtra = azimuth
        )
        is LightData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = illuminance
        )
        is GpsData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = latitude.toFloat(),
            valueY = longitude.toFloat(),
            valueZ = altitude.toFloat(),
            valueExtra = speed,
            accuracy = accuracy
        )
        is ProximityData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = distance,
            valueExtra = if (isNear) 1f else 0f
        )
        is BarometerData -> SensorReading(
            timestamp = timestamp,
            sensorType = sensorType.name,
            valueX = pressure,
            valueExtra = altitude
        )
    }
}
