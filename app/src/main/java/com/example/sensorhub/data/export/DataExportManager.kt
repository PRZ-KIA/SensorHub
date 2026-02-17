package com.example.sensorhub.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.sensorhub.data.model.SensorReading
import com.example.sensorhub.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data Export Manager
 * Handles exporting sensor data to CSV and JSON formats
 */
class DataExportManager(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    
    /**
     * Export data to CSV format
     */
    suspend fun exportToCsv(
        readings: List<SensorReading>,
        fileName: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val file = createExportFile(fileName ?: generateFileName("csv"), "csv")
            val writer = FileWriter(file)
            
            // Write CSV header
            writer.append("ID,Sensor Type,X,Y,Z,Timestamp,Human Readable Time\n")
            
            // Write data rows
            readings.forEach { reading ->
                writer.append("${reading.id},")
                writer.append("${reading.sensorType},")
                writer.append("${reading.x},")
                writer.append("${reading.y},")
                writer.append("${reading.z},")
                writer.append("${reading.timestamp},")
                writer.append("${dateFormat.format(Date(reading.timestamp))}\n")
            }
            
            writer.flush()
            writer.close()
            
            ExportResult.Success(
                file = file,
                format = ExportFormat.CSV,
                recordCount = readings.size,
                fileSizeBytes = file.length()
            )
        } catch (e: Exception) {
            ErrorHandler.logError(
                tag = "DataExportManager",
                message = "Failed to export to CSV",
                throwable = e
            )
            ExportResult.Error(e.message ?: "Export failed")
        }
    }
    
    /**
     * Export data to JSON format
     */
    suspend fun exportToJson(
        readings: List<SensorReading>,
        fileName: String? = null,
        prettyPrint: Boolean = true
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val file = createExportFile(fileName ?: generateFileName("json"), "json")
            
            val jsonObject = JSONObject().apply {
                put("exportDate", dateFormat.format(Date()))
                put("recordCount", readings.size)
                put("appVersion", "3.0.0-alpha")
                
                val dataArray = JSONArray()
                readings.forEach { reading ->
                    val readingObj = JSONObject().apply {
                        put("id", reading.id)
                        put("sensorType", reading.sensorType)
                        put("x", reading.x)
                        put("y", reading.y)
                        put("z", reading.z)
                        put("timestamp", reading.timestamp)
                        put("timestampReadable", dateFormat.format(Date(reading.timestamp)))
                    }
                    dataArray.put(readingObj)
                }
                put("data", dataArray)
            }
            
            val jsonString = if (prettyPrint) {
                jsonObject.toString(4)
            } else {
                jsonObject.toString()
            }
            
            file.writeText(jsonString)
            
            ExportResult.Success(
                file = file,
                format = ExportFormat.JSON,
                recordCount = readings.size,
                fileSizeBytes = file.length()
            )
        } catch (e: Exception) {
            ErrorHandler.logError(
                tag = "DataExportManager",
                message = "Failed to export to JSON",
                throwable = e
            )
            ExportResult.Error(e.message ?: "Export failed")
        }
    }
    
    /**
     * Export statistics summary
     */
    suspend fun exportStatistics(
        readings: List<SensorReading>,
        fileName: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val file = createExportFile(fileName ?: generateFileName("stats_json"), "json")
            
            val stats = calculateStatistics(readings)
            val jsonObject = JSONObject(stats)
            
            file.writeText(jsonObject.toString(4))
            
            ExportResult.Success(
                file = file,
                format = ExportFormat.JSON,
                recordCount = 1,
                fileSizeBytes = file.length()
            )
        } catch (e: Exception) {
            ErrorHandler.logError(
                tag = "DataExportManager",
                message = "Failed to export statistics",
                throwable = e
            )
            ExportResult.Error(e.message ?: "Export failed")
        }
    }
    
    /**
     * Share exported file
     */
    fun shareFile(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = when (file.extension) {
                "csv" -> "text/csv"
                "json" -> "application/json"
                else -> "text/plain"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Get all export files
     */
    fun getExportFiles(): List<File> {
        val exportDir = getExportDirectory()
        return exportDir.listFiles()?.toList() ?: emptyList()
    }
    
    /**
     * Delete export file
     */
    fun deleteExportFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            ErrorHandler.logError(
                tag = "DataExportManager",
                message = "Failed to delete export file",
                throwable = e
            )
            false
        }
    }
    
    /**
     * Clear all export files
     */
    fun clearAllExports(): Int {
        val files = getExportFiles()
        var deletedCount = 0
        
        files.forEach { file ->
            if (file.delete()) {
                deletedCount++
            }
        }
        
        return deletedCount
    }
    
    /**
     * Get total size of export files
     */
    fun getTotalExportSize(): Long {
        return getExportFiles().sumOf { it.length() }
    }
    
    // Private helper methods
    
    private fun createExportFile(fileName: String, extension: String): File {
        val exportDir = getExportDirectory()
        return File(exportDir, "$fileName.$extension")
    }
    
    private fun getExportDirectory(): File {
        val dir = File(context.getExternalFilesDir(null), "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    private fun generateFileName(extension: String): String {
        val timestamp = fileNameFormat.format(Date())
        return "sensorhub_export_$timestamp"
    }
    
    private fun calculateStatistics(readings: List<SensorReading>): Map<String, Any> {
        val grouped = readings.groupBy { it.sensorType }
        
        return mapOf(
            "exportDate" to dateFormat.format(Date()),
            "totalReadings" to readings.size,
            "sensorTypes" to grouped.keys.toList(),
            "dateRange" to mapOf(
                "oldest" to dateFormat.format(Date(readings.minOfOrNull { it.timestamp } ?: 0)),
                "newest" to dateFormat.format(Date(readings.maxOfOrNull { it.timestamp } ?: 0))
            ),
            "byType" to grouped.mapValues { (_, typeReadings) ->
                mapOf(
                    "count" to typeReadings.size,
                    "xStats" to getValueStats(typeReadings.map { it.x }),
                    "yStats" to getValueStats(typeReadings.map { it.y }),
                    "zStats" to getValueStats(typeReadings.map { it.z })
                )
            }
        )
    }
    
    private fun getValueStats(values: List<Float>): Map<String, Float> {
        if (values.isEmpty()) {
            return mapOf(
                "min" to 0f,
                "max" to 0f,
                "avg" to 0f,
                "std" to 0f
            )
        }
        
        val avg = values.average().toFloat()
        val variance = values.map { (it - avg) * (it - avg) }.average().toFloat()
        val std = kotlin.math.sqrt(variance)
        
        return mapOf(
            "min" to (values.minOrNull() ?: 0f),
            "max" to (values.maxOrNull() ?: 0f),
            "avg" to avg,
            "std" to std
        )
    }
}

/**
 * Export format enum
 */
enum class ExportFormat {
    CSV,
    JSON,
    STATISTICS
}

/**
 * Export result sealed class
 */
sealed class ExportResult {
    data class Success(
        val file: File,
        val format: ExportFormat,
        val recordCount: Int,
        val fileSizeBytes: Long
    ) : ExportResult() {
        fun getFileSizeFormatted(): String {
            return when {
                fileSizeBytes < 1024 -> "$fileSizeBytes B"
                fileSizeBytes < 1024 * 1024 -> "${fileSizeBytes / 1024} KB"
                else -> "${fileSizeBytes / (1024 * 1024)} MB"
            }
        }
    }
    
    data class Error(val message: String) : ExportResult()
}

/**
 * Export configuration
 */
data class ExportConfig(
    val format: ExportFormat = ExportFormat.CSV,
    val includeStatistics: Boolean = false,
    val prettyPrintJson: Boolean = true,
    val dateRange: DateRange? = null,
    val sensorTypes: List<String>? = null
)

/**
 * Date range for filtering
 */
data class DateRange(
    val startTime: Long,
    val endTime: Long
) {
    fun contains(timestamp: Long): Boolean {
        return timestamp in startTime..endTime
    }
}

/**
 * Export extensions for SensorReading list
 */
suspend fun List<SensorReading>.exportToCsv(
    context: Context,
    fileName: String? = null
): ExportResult {
    return DataExportManager(context).exportToCsv(this, fileName)
}

suspend fun List<SensorReading>.exportToJson(
    context: Context,
    fileName: String? = null,
    prettyPrint: Boolean = true
): ExportResult {
    return DataExportManager(context).exportToJson(this, fileName, prettyPrint)
}

/**
 * File format detector
 */
object FileFormatDetector {
    fun detectFormat(file: File): ExportFormat? {
        return when (file.extension.lowercase()) {
            "csv" -> ExportFormat.CSV
            "json" -> ExportFormat.JSON
            else -> null
        }
    }
    
    fun isValidExportFile(file: File): Boolean {
        return detectFormat(file) != null
    }
}
