package com.kia.sensorhub.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.data.model.*
import com.kia.sensorhub.data.repository.SensorRepository
import com.kia.sensorhub.sensors.SensorInfo
import com.kia.sensorhub.ui.components.SensorCard
import com.kia.sensorhub.ui.components.SensorInfoDialog
import com.kia.sensorhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Light Sensor Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightSensorScreen(
    viewModel: LightSensorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Light Sensor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorLight,
                    titleContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sensor Info",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isAvailable) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isAvailable) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.Error,
                            contentDescription = null
                        )
                        Text(
                            text = if (uiState.isAvailable) "Sensor Available" else "Sensor Not Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (uiState.isMonitoring) {
                        Text(
                            text = "Light Level: ${uiState.lightLevel}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Light Visualization
            Text(
                text = "Ambient Light",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            LightVisualization(
                illuminance = uiState.currentData.illuminance,
                level = uiState.lightLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            
            // Current Value
            SensorCard(
                label = "Illuminance",
                value = uiState.currentData.illuminance,
                unit = "lux",
                color = SensorLight
            )
            
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        if (uiState.isMonitoring) {
                            viewModel.stopMonitoring()
                        } else {
                            viewModel.startMonitoring()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isMonitoring) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    ),
                    enabled = uiState.isAvailable
                ) {
                    Icon(
                        imageVector = if (uiState.isMonitoring) 
                            Icons.Default.Stop 
                        else 
                            Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.isMonitoring) "Stop" else "Start")
                }
            }
        }
    }
    
    if (showInfoDialog && uiState.sensorInfo != null) {
        SensorInfoDialog(
            sensorInfo = uiState.sensorInfo!!,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun LightVisualization(
    illuminance: Float,
    level: String,
    modifier: Modifier = Modifier
) {
    val animatedIlluminance by animateFloatAsState(
        targetValue = illuminance,
        label = "illuminance"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = minOf(size.width, size.height) / 2 * 0.8f
                
                // Calculate radius based on light level (log scale for better visualization)
                val normalizedLight = (animatedIlluminance / 10000f).coerceIn(0f, 1f)
                val radius = maxRadius * (0.2f + 0.8f * normalizedLight)
                
                // Draw glowing circle
                val gradient = Brush.radialGradient(
                    colors = listOf(
                        Color.Yellow.copy(alpha = 0.8f),
                        Color.Yellow.copy(alpha = 0.4f),
                        Color.Yellow.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                )
                
                drawCircle(
                    brush = gradient,
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
                
                // Draw sun rays if bright
                if (animatedIlluminance > 1000) {
                    for (i in 0..11) {
                        val angle = (i * 30f) * Math.PI / 180f
                        val startX = centerX + (radius * 0.8f * kotlin.math.cos(angle)).toFloat()
                        val startY = centerY + (radius * 0.8f * kotlin.math.sin(angle)).toFloat()
                        val endX = centerX + (radius * 1.2f * kotlin.math.cos(angle)).toFloat()
                        val endY = centerY + (radius * 1.2f * kotlin.math.sin(angle)).toFloat()
                        
                        drawLine(
                            color = Color.Yellow.copy(alpha = 0.6f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 4f
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when {
                        illuminance < 10 -> Icons.Default.DarkMode
                        illuminance < 200 -> Icons.Default.Brightness4
                        else -> Icons.Default.LightMode
                    },
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = when {
                        illuminance < 10 -> Color.Gray
                        illuminance < 200 -> Color(0xFFFFEB3B)
                        else -> Color.Yellow
                    }
                )
                Text(
                    text = level,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${animatedIlluminance.roundToInt()} lux",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

/**
 * Proximity Sensor Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProximityScreen(
    viewModel: ProximityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proximity Sensor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SensorProximity,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sensor Info",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.currentData.isNear)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.currentData.isNear)
                            Icons.Default.Warning
                        else
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Column {
                        Text(
                            text = if (uiState.currentData.isNear) "NEAR" else "FAR",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.currentData.isNear)
                                "Object detected nearby"
                            else
                                "No object nearby",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Distance
            SensorCard(
                label = "Distance",
                value = uiState.currentData.distance,
                unit = "cm",
                color = SensorProximity
            )
            
            // Controls
            Button(
                onClick = { 
                    if (uiState.isMonitoring) {
                        viewModel.stopMonitoring()
                    } else {
                        viewModel.startMonitoring()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isMonitoring) 
                        MaterialTheme.colorScheme.error 
                        else 
                        MaterialTheme.colorScheme.primary
                ),
                enabled = uiState.isAvailable
            ) {
                Icon(
                    imageVector = if (uiState.isMonitoring) 
                        Icons.Default.Stop 
                    else 
                        Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.isMonitoring) "Stop" else "Start")
            }
        }
    }
    
    if (showInfoDialog && uiState.sensorInfo != null) {
        SensorInfoDialog(
            sensorInfo = uiState.sensorInfo!!,
            onDismiss = { showInfoDialog = false }
        )
    }
}

// ViewModels for new screens
@HiltViewModel
class LightSensorViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LightSensorUiState())
    val uiState: StateFlow<LightSensorUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        _uiState.value = _uiState.value.copy(isAvailable = false) // TODO: Add to repository
    }
    
    fun startMonitoring() {
        // TODO: Implement when LightSensorManager is added to repository
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
}

data class LightSensorUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: LightData = LightData(),
    val lightLevel: String = "Unknown",
    val sensorInfo: SensorInfo? = null
)

@HiltViewModel
class ProximityViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProximityUiState())
    val uiState: StateFlow<ProximityUiState> = _uiState.asStateFlow()
    
    private var monitoringJob: Job? = null
    
    init {
        checkSensorAvailability()
    }
    
    private fun checkSensorAvailability() {
        _uiState.value = _uiState.value.copy(isAvailable = false) // TODO: Add to repository
    }
    
    fun startMonitoring() {
        // TODO: Implement
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
}

data class ProximityUiState(
    val isAvailable: Boolean = false,
    val isMonitoring: Boolean = false,
    val currentData: ProximityData = ProximityData(),
    val sensorInfo: SensorInfo? = null
)
