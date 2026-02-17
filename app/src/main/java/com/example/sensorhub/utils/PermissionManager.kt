package com.example.sensorhub.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Permission Manager for handling runtime permissions
 */
class PermissionManager(private val activity: ComponentActivity) {
    
    /**
     * Location permissions
     */
    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    /**
     * Audio permissions
     */
    val audioPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    
    /**
     * Storage permissions (API < 33)
     */
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    /**
     * Notification permissions (API 33+)
     */
    val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }
    
    /**
     * Check if all permissions in array are granted
     */
    fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return hasPermissions(locationPermissions)
    }
    
    /**
     * Check if audio permission is granted
     */
    fun hasAudioPermission(): Boolean {
        return hasPermissions(audioPermissions)
    }
    
    /**
     * Check if storage permissions are granted
     */
    fun hasStoragePermission(): Boolean {
        return hasPermissions(storagePermissions)
    }
    
    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermissions(notificationPermissions)
        } else {
            true // Not required before Android 13
        }
    }
    
    /**
     * Request single permission
     */
    fun requestPermission(
        permission: String,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }
        launcher.launch(permission)
    }
    
    /**
     * Request multiple permissions
     */
    fun requestPermissions(
        permissions: Array<String>,
        onAllGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val denied = results.filter { !it.value }.keys.toList()
            if (denied.isEmpty()) {
                onAllGranted()
            } else {
                onDenied(denied)
            }
        }
        launcher.launch(permissions)
    }
    
    /**
     * Check if permission should show rationale
     */
    fun shouldShowRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * Get permission status description
     */
    fun getPermissionStatus(permission: String): PermissionStatus {
        return when {
            hasPermissions(arrayOf(permission)) -> PermissionStatus.GRANTED
            shouldShowRationale(permission) -> PermissionStatus.DENIED_SHOW_RATIONALE
            else -> PermissionStatus.DENIED_DONT_ASK_AGAIN
        }
    }
}

/**
 * Permission status enum
 */
enum class PermissionStatus {
    GRANTED,
    DENIED_SHOW_RATIONALE,
    DENIED_DONT_ASK_AGAIN
}

/**
 * Permission request result
 */
sealed class PermissionResult {
    object Granted : PermissionResult()
    data class Denied(val permissions: List<String>) : PermissionResult()
    data class PermanentlyDenied(val permissions: List<String>) : PermissionResult()
}

/**
 * Extension function to check permission
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Extension function to check multiple permissions
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions.all { hasPermission(it) }
}

/**
 * Get required permissions for feature
 */
object RequiredPermissions {
    
    fun forGPS(): Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    fun forVoiceRecognition(): Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    
    fun forNotifications(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
    
    fun forBackgroundLocation(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            emptyArray()
        }
    }
}

/**
 * Permission utility functions
 */
object PermissionUtils {
    
    /**
     * Get user-friendly permission name
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> "Precise Location"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "Background Location"
            Manifest.permission.RECORD_AUDIO -> "Microphone"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage (Read)"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Storage (Write)"
            Manifest.permission.CAMERA -> "Camera"
            else -> permission.substringAfterLast('.')
        }
    }
    
    /**
     * Get permission description
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> 
                "Required for GPS sensor functionality and location tracking"
            
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> 
                "Allows location tracking even when app is in background"
            
            Manifest.permission.RECORD_AUDIO -> 
                "Required for voice recognition feature"
            
            Manifest.permission.POST_NOTIFICATIONS -> 
                "Allows app to show sensor alerts and insights"
            
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> 
                "Required for exporting sensor data to files"
            
            else -> "Required for app functionality"
        }
    }
    
    /**
     * Check if permission is dangerous (requires runtime request)
     */
    fun isDangerousPermission(permission: String): Boolean {
        val dangerousPermissions = setOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.CALL_PHONE
        )
        
        return permission in dangerousPermissions ||
               (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                permission == Manifest.permission.POST_NOTIFICATIONS)
    }
}
