package com.example.sensorhub.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.sensorhub.data.model.LightData
import com.example.sensorhub.data.model.ProximityData
import com.example.sensorhub.data.model.BarometerData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.pow

/**
 * Manager for light sensor
 * Provides ambient light level data
 */
class LightSensorManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    fun isAvailable(): Boolean = lightSensor != null
    
    fun getLightFlow(
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<LightData> = callbackFlow {
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    val illuminance = event.values[0] // lux
                    
                    val data = LightData(
                        timestamp = System.currentTimeMillis(),
                        illuminance = illuminance
                    )
                    
                    trySend(data)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        lightSensor?.let {
            sensorManager.registerListener(listener, it, samplingPeriodUs)
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    fun getSensorInfo(): SensorInfo? {
        return lightSensor?.let {
            SensorInfo(
                name = it.name,
                vendor = it.vendor,
                version = it.version,
                type = it.type,
                maxRange = it.maximumRange,
                resolution = it.resolution,
                power = it.power,
                minDelay = it.minDelay
            )
        }
    }
    
    /**
     * Get light level description
     */
    fun getLightLevelDescription(lux: Float): String {
        return when {
            lux < 1 -> "Dark"
            lux < 10 -> "Very Dim"
            lux < 50 -> "Dim"
            lux < 200 -> "Moderate"
            lux < 1000 -> "Bright"
            lux < 10000 -> "Very Bright"
            else -> "Extremely Bright"
        }
    }
}

/**
 * Manager for proximity sensor
 * Detects nearby objects
 */
class ProximitySensorManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    
    fun isAvailable(): Boolean = proximitySensor != null
    
    fun getProximityFlow(
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<ProximityData> = callbackFlow {
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    val distance = event.values[0] // cm
                    val maxRange = event.sensor.maximumRange
                    
                    // Determine if object is near (typically < 5cm)
                    val isNear = distance < maxRange / 2
                    
                    val data = ProximityData(
                        timestamp = System.currentTimeMillis(),
                        distance = distance,
                        isNear = isNear
                    )
                    
                    trySend(data)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        proximitySensor?.let {
            sensorManager.registerListener(listener, it, samplingPeriodUs)
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    fun getSensorInfo(): SensorInfo? {
        return proximitySensor?.let {
            SensorInfo(
                name = it.name,
                vendor = it.vendor,
                version = it.version,
                type = it.type,
                maxRange = it.maximumRange,
                resolution = it.resolution,
                power = it.power,
                minDelay = it.minDelay
            )
        }
    }
}

/**
 * Manager for barometer (pressure sensor)
 * Measures atmospheric pressure
 */
class BarometerManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    
    fun isAvailable(): Boolean = pressureSensor != null
    
    fun getBarometerFlow(
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<BarometerData> = callbackFlow {
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                    val pressure = event.values[0] // hPa (millibar)
                    
                    // Calculate altitude from pressure (approximate)
                    val altitude = calculateAltitude(pressure)
                    
                    val data = BarometerData(
                        timestamp = System.currentTimeMillis(),
                        pressure = pressure,
                        altitude = altitude
                    )
                    
                    trySend(data)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        pressureSensor?.let {
            sensorManager.registerListener(listener, it, samplingPeriodUs)
        }
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    fun getSensorInfo(): SensorInfo? {
        return pressureSensor?.let {
            SensorInfo(
                name = it.name,
                vendor = it.vendor,
                version = it.version,
                type = it.type,
                maxRange = it.maximumRange,
                resolution = it.resolution,
                power = it.power,
                minDelay = it.minDelay
            )
        }
    }
    
    /**
     * Calculate altitude from atmospheric pressure
     * Using international barometric formula
     */
    private fun calculateAltitude(pressure: Float): Float {
        val seaLevelPressure = 1013.25f // hPa
        return 44330 * (1.0 - (pressure / seaLevelPressure).pow(0.1903)).toFloat()
    }
    
    /**
     * Get weather trend from pressure
     */
    fun getWeatherTrend(currentPressure: Float, previousPressure: Float): String {
        val change = currentPressure - previousPressure
        return when {
            change > 2 -> "Rising (Improving weather)"
            change > 0.5 -> "Slightly rising"
            change < -2 -> "Falling (Worsening weather)"
            change < -0.5 -> "Slightly falling"
            else -> "Stable"
        }
    }
}
