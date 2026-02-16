package com.example.sensorhub.data.repository

import com.example.sensorhub.data.database.SensorDao
import com.example.sensorhub.data.model.AccelerometerData
import com.example.sensorhub.data.model.GyroscopeData
import com.example.sensorhub.data.model.MagnetometerData
import com.example.sensorhub.data.model.SensorData
import com.example.sensorhub.data.model.SensorReading
import com.example.sensorhub.data.model.toSensorReading
import com.example.sensorhub.sensors.AccelerometerManager
import com.example.sensorhub.sensors.GyroscopeManager
import com.example.sensorhub.sensors.MagnetometerManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for accessing sensor data
 * Abstracts data sources (sensors and database) from ViewModels
 */
@Singleton
class SensorRepository @Inject constructor(
    private val sensorDao: SensorDao,
    private val accelerometerManager: AccelerometerManager,
    private val gyroscopeManager: GyroscopeManager,
    private val magnetometerManager: MagnetometerManager
) {
    
    // ============ Accelerometer ============
    
    fun isAccelerometerAvailable(): Boolean = 
        accelerometerManager.isAvailable()
    
    fun getAccelerometerFlow(): Flow<AccelerometerData> = 
        accelerometerManager.getAccelerometerFlow()
    
    fun getAccelerometerInfo() = 
        accelerometerManager.getSensorInfo()
    
    // ============ Gyroscope ============
    
    fun isGyroscopeAvailable(): Boolean = 
        gyroscopeManager.isAvailable()
    
    fun getGyroscopeFlow(): Flow<GyroscopeData> = 
        gyroscopeManager.getGyroscopeFlow()
    
    fun getGyroscopeInfo() = 
        gyroscopeManager.getSensorInfo()
    
    // ============ Magnetometer ============
    
    fun isMagnetometerAvailable(): Boolean = 
        magnetometerManager.isAvailable()
    
    fun getMagnetometerFlow(): Flow<MagnetometerData> = 
        magnetometerManager.getMagnetometerFlow()
    
    fun getMagnetometerInfo() = 
        magnetometerManager.getSensorInfo()
    
    // ============ Database Operations ============
    
    /**
     * Save sensor reading to database
     */
    suspend fun saveSensorReading(sensorData: SensorData) {
        val reading = sensorData.toSensorReading()
        sensorDao.insertReading(reading)
    }
    
    /**
     * Save multiple sensor readings to database
     */
    suspend fun saveSensorReadings(sensorDataList: List<SensorData>) {
        val readings = sensorDataList.map { it.toSensorReading() }
        sensorDao.insertReadings(readings)
    }
    
    /**
     * Get all sensor readings
     */
    fun getAllReadings(): Flow<List<SensorReading>> = 
        sensorDao.getAllReadingsFlow()
    
    /**
     * Get readings for a specific sensor type
     */
    fun getReadingsBySensorType(sensorType: String, limit: Int = 100): Flow<List<SensorReading>> = 
        sensorDao.getReadingsBySensorType(sensorType, limit)
    
    /**
     * Get readings within a time range
     */
    fun getReadingsByTimeRange(startTime: Long, endTime: Long): Flow<List<SensorReading>> = 
        sensorDao.getReadingsByTimeRange(startTime, endTime)
    
    /**
     * Get latest reading for a sensor type
     */
    suspend fun getLatestReading(sensorType: String): SensorReading? = 
        sensorDao.getLatestReading(sensorType)
    
    /**
     * Delete all readings
     */
    suspend fun deleteAllReadings() {
        sensorDao.deleteAllReadings()
    }
    
    /**
     * Delete old readings (older than timestamp)
     */
    suspend fun deleteOldReadings(timestamp: Long) {
        sensorDao.deleteOldReadings(timestamp)
    }
    
    /**
     * Get total count of readings
     */
    suspend fun getReadingsCount(): Int = 
        sensorDao.getReadingsCount()
    
    /**
     * Save a list of sensor readings
     */
    suspend fun saveSensorReadings(readings: List<SensorData>) {
        readings.forEach { reading ->
            saveSensorReading(reading)
        }
    }
}
