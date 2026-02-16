package com.example.sensorhub.affective

import com.example.sensorhub.data.model.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Affective Computing Module
 * Analyzes user emotional state based on sensor data
 */

/**
 * Emotion types detected by the system
 */
enum class EmotionType {
    CALM,
    STRESSED,
    ACTIVE,
    RESTING,
    ANXIOUS,
    FOCUSED,
    DISTRACTED,
    UNKNOWN
}

/**
 * Detected emotion with confidence level
 */
data class DetectedEmotion(
    val emotion: EmotionType,
    val confidence: Float, // 0.0 to 1.0
    val timestamp: Long = System.currentTimeMillis(),
    val factors: Map<String, Float> = emptyMap() // Contributing factors
)

/**
 * Affective state representing overall emotional condition
 */
data class AffectiveState(
    val arousal: Float, // 0.0 (calm) to 1.0 (excited)
    val valence: Float, // 0.0 (negative) to 1.0 (positive)
    val stress: Float, // 0.0 (relaxed) to 1.0 (stressed)
    val focus: Float, // 0.0 (distracted) to 1.0 (focused)
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Main Affective Analyzer
 */
class AffectiveAnalyzer {
    
    private val accelerometerHistory = mutableListOf<AccelerometerData>()
    private val gyroscopeHistory = mutableListOf<GyroscopeData>()
    private val touchEventHistory = mutableListOf<TouchEvent>()
    
    companion object {
        private const val HISTORY_SIZE = 100
        private const val MOVEMENT_THRESHOLD_CALM = 2.0f
        private const val MOVEMENT_THRESHOLD_ACTIVE = 8.0f
        private const val ROTATION_THRESHOLD_STABLE = 0.1f
    }
    
    /**
     * Analyze emotion from accelerometer data
     */
    fun analyzeFromAccelerometer(data: AccelerometerData): DetectedEmotion {
        accelerometerHistory.add(data)
        if (accelerometerHistory.size > HISTORY_SIZE) {
            accelerometerHistory.removeAt(0)
        }
        
        if (accelerometerHistory.size < 20) {
            return DetectedEmotion(EmotionType.UNKNOWN, 0.0f)
        }
        
        val movementIntensity = calculateMovementIntensity(accelerometerHistory)
        val movementVariability = calculateVariability(
            accelerometerHistory.map { it.magnitude }
        )
        
        // Determine emotion based on movement patterns
        return when {
            movementIntensity < MOVEMENT_THRESHOLD_CALM && movementVariability < 0.5f -> {
                DetectedEmotion(
                    emotion = EmotionType.CALM,
                    confidence = 0.8f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
            movementIntensity > MOVEMENT_THRESHOLD_ACTIVE && movementVariability > 2.0f -> {
                DetectedEmotion(
                    emotion = EmotionType.STRESSED,
                    confidence = 0.7f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
            movementIntensity > MOVEMENT_THRESHOLD_ACTIVE && movementVariability < 1.0f -> {
                DetectedEmotion(
                    emotion = EmotionType.ACTIVE,
                    confidence = 0.75f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
            else -> {
                DetectedEmotion(
                    emotion = EmotionType.RESTING,
                    confidence = 0.6f,
                    factors = mapOf(
                        "movement_intensity" to movementIntensity,
                        "variability" to movementVariability
                    )
                )
            }
        }
    }
    
    /**
     * Analyze emotion from gyroscope data
     */
    fun analyzeFromGyroscope(data: GyroscopeData): DetectedEmotion {
        gyroscopeHistory.add(data)
        if (gyroscopeHistory.size > HISTORY_SIZE) {
            gyroscopeHistory.removeAt(0)
        }
        
        if (gyroscopeHistory.size < 20) {
            return DetectedEmotion(EmotionType.UNKNOWN, 0.0f)
        }
        
        val rotationStability = calculateRotationStability(gyroscopeHistory)
        val rotationIntensity = gyroscopeHistory.takeLast(10)
            .map { it.rotationRate }
            .average()
            .toFloat()
        
        return when {
            rotationStability > 0.8f && rotationIntensity < ROTATION_THRESHOLD_STABLE -> {
                DetectedEmotion(
                    emotion = EmotionType.FOCUSED,
                    confidence = 0.75f,
                    factors = mapOf(
                        "stability" to rotationStability,
                        "intensity" to rotationIntensity
                    )
                )
            }
            rotationStability < 0.4f -> {
                DetectedEmotion(
                    emotion = EmotionType.DISTRACTED,
                    confidence = 0.7f,
                    factors = mapOf(
                        "stability" to rotationStability,
                        "intensity" to rotationIntensity
                    )
                )
            }
            else -> {
                DetectedEmotion(
                    emotion = EmotionType.RESTING,
                    confidence = 0.5f,
                    factors = mapOf(
                        "stability" to rotationStability,
                        "intensity" to rotationIntensity
                    )
                )
            }
        }
    }
    
    /**
     * Analyze touch interaction patterns
     */
    fun analyzeFromTouchEvents(events: List<TouchEvent>): DetectedEmotion {
        touchEventHistory.addAll(events)
        if (touchEventHistory.size > HISTORY_SIZE) {
            touchEventHistory.subList(0, touchEventHistory.size - HISTORY_SIZE).clear()
        }
        
        if (touchEventHistory.size < 10) {
            return DetectedEmotion(EmotionType.UNKNOWN, 0.0f)
        }
        
        val avgDuration = touchEventHistory.map { it.duration }.average().toFloat()
        val avgPressure = touchEventHistory.map { it.pressure }.average().toFloat()
        val frequency = touchEventHistory.size.toFloat() / 60f // per minute
        
        return when {
            avgPressure > 0.7f && frequency > 30f -> {
                DetectedEmotion(
                    emotion = EmotionType.STRESSED,
                    confidence = 0.7f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
            avgPressure < 0.3f && avgDuration > 500f -> {
                DetectedEmotion(
                    emotion = EmotionType.CALM,
                    confidence = 0.65f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
            frequency > 40f && avgDuration < 200f -> {
                DetectedEmotion(
                    emotion = EmotionType.ANXIOUS,
                    confidence = 0.6f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
            else -> {
                DetectedEmotion(
                    emotion = EmotionType.RESTING,
                    confidence = 0.5f,
                    factors = mapOf(
                        "pressure" to avgPressure,
                        "frequency" to frequency,
                        "duration" to avgDuration
                    )
                )
            }
        }
    }
    
    /**
     * Combine multiple emotions into an affective state
     */
    fun computeAffectiveState(emotions: List<DetectedEmotion>): AffectiveState {
        if (emotions.isEmpty()) {
            return AffectiveState(
                arousal = 0.5f,
                valence = 0.5f,
                stress = 0.5f,
                focus = 0.5f
            )
        }
        
        // Map emotions to dimensional values
        var totalArousal = 0f
        var totalValence = 0f
        var totalStress = 0f
        var totalFocus = 0f
        var totalWeight = 0f
        
        emotions.forEach { emotion ->
            val weight = emotion.confidence
            totalWeight += weight
            
            when (emotion.emotion) {
                EmotionType.CALM -> {
                    totalArousal += 0.2f * weight
                    totalValence += 0.7f * weight
                    totalStress += 0.1f * weight
                    totalFocus += 0.6f * weight
                }
                EmotionType.STRESSED -> {
                    totalArousal += 0.8f * weight
                    totalValence += 0.3f * weight
                    totalStress += 0.9f * weight
                    totalFocus += 0.4f * weight
                }
                EmotionType.ACTIVE -> {
                    totalArousal += 0.9f * weight
                    totalValence += 0.6f * weight
                    totalStress += 0.5f * weight
                    totalFocus += 0.7f * weight
                }
                EmotionType.RESTING -> {
                    totalArousal += 0.3f * weight
                    totalValence += 0.5f * weight
                    totalStress += 0.2f * weight
                    totalFocus += 0.5f * weight
                }
                EmotionType.ANXIOUS -> {
                    totalArousal += 0.7f * weight
                    totalValence += 0.2f * weight
                    totalStress += 0.8f * weight
                    totalFocus += 0.3f * weight
                }
                EmotionType.FOCUSED -> {
                    totalArousal += 0.6f * weight
                    totalValence += 0.7f * weight
                    totalStress += 0.3f * weight
                    totalFocus += 0.9f * weight
                }
                EmotionType.DISTRACTED -> {
                    totalArousal += 0.5f * weight
                    totalValence += 0.4f * weight
                    totalStress += 0.6f * weight
                    totalFocus += 0.2f * weight
                }
                EmotionType.UNKNOWN -> {
                    // Skip unknown emotions
                }
            }
        }
        
        return if (totalWeight > 0) {
            AffectiveState(
                arousal = (totalArousal / totalWeight).coerceIn(0f, 1f),
                valence = (totalValence / totalWeight).coerceIn(0f, 1f),
                stress = (totalStress / totalWeight).coerceIn(0f, 1f),
                focus = (totalFocus / totalWeight).coerceIn(0f, 1f)
            )
        } else {
            AffectiveState(
                arousal = 0.5f,
                valence = 0.5f,
                stress = 0.5f,
                focus = 0.5f
            )
        }
    }
    
    /**
     * Calculate movement intensity from accelerometer history
     */
    private fun calculateMovementIntensity(history: List<AccelerometerData>): Float {
        if (history.size < 2) return 0f
        
        var totalChange = 0f
        for (i in 1 until history.size) {
            val prev = history[i - 1]
            val curr = history[i]
            
            val dx = curr.x - prev.x
            val dy = curr.y - prev.y
            val dz = curr.z - prev.z
            
            totalChange += sqrt(dx * dx + dy * dy + dz * dz)
        }
        
        return totalChange / history.size
    }
    
    /**
     * Calculate variability (standard deviation) of values
     */
    private fun calculateVariability(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean).pow(2) }.average().toFloat()
        
        return sqrt(variance)
    }
    
    /**
     * Calculate rotation stability from gyroscope history
     */
    private fun calculateRotationStability(history: List<GyroscopeData>): Float {
        if (history.size < 2) return 0f
        
        val variability = calculateVariability(history.map { it.rotationRate })
        val maxVariability = 2.0f // rad/s
        
        // Higher stability = lower variability
        return (1f - (variability / maxVariability).coerceIn(0f, 1f))
    }
    
    /**
     * Clear all history
     */
    fun clearHistory() {
        accelerometerHistory.clear()
        gyroscopeHistory.clear()
        touchEventHistory.clear()
    }
}

/**
 * Touch event data for analysis
 */
data class TouchEvent(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val pressure: Float, // 0.0 to 1.0
    val duration: Float // milliseconds
)

/**
 * Emotion history tracker
 */
class EmotionTracker {
    private val emotionHistory = mutableListOf<DetectedEmotion>()
    private val stateHistory = mutableListOf<AffectiveState>()
    
    companion object {
        private const val MAX_HISTORY = 1000
    }
    
    fun addEmotion(emotion: DetectedEmotion) {
        emotionHistory.add(emotion)
        if (emotionHistory.size > MAX_HISTORY) {
            emotionHistory.removeAt(0)
        }
    }
    
    fun addState(state: AffectiveState) {
        stateHistory.add(state)
        if (stateHistory.size > MAX_HISTORY) {
            stateHistory.removeAt(0)
        }
    }
    
    fun getEmotionHistory(): List<DetectedEmotion> = emotionHistory.toList()
    
    fun getStateHistory(): List<AffectiveState> = stateHistory.toList()
    
    fun getEmotionDistribution(): Map<EmotionType, Int> {
        return emotionHistory
            .groupingBy { it.emotion }
            .eachCount()
    }
    
    fun getAverageArousal(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.arousal }.average().toFloat()
    }
    
    fun getAverageValence(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.valence }.average().toFloat()
    }
    
    fun getAverageStress(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.stress }.average().toFloat()
    }
    
    fun getAverageFocus(): Float {
        return if (stateHistory.isEmpty()) 0.5f
        else stateHistory.map { it.focus }.average().toFloat()
    }
    
    fun clear() {
        emotionHistory.clear()
        stateHistory.clear()
    }
}
