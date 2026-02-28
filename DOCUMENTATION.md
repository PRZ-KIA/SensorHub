# SensorHub - Developer Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Module Structure](#module-structure)
3. [Sensor Integration](#sensor-integration)
4. [Database Design](#database-design)
5. [UI Components](#ui-components)
6. [Testing Strategy](#testing-strategy)
7. [Best Practices](#best-practices)

## Architecture Overview

SensorHub follows the MVVM (Model-View-ViewModel) architecture pattern with clean separation of concerns:

### Layers

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  (Jetpack Compose + ViewModels)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Domain Layer                 │
│     (Repository Pattern)            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Data Layer                  │
│  (Room Database + Sensor Managers)  │
└─────────────────────────────────────┘
```

## Module Structure

### Data Module
Handles all data operations including sensor readings and database persistence.

**Key Classes:**
- `SensorData.kt` - Data models for all sensors, including the `SensorReading` Room entity
- `SensorDao.kt` - Database access interface
- `SensorDatabase.kt` - Room database definition

### Sensors Module
Manages interaction with device hardware sensors.

**Key Classes:**
- `AccelerometerManager.kt` - Accelerometer sensor manager
- `GyroscopeManager.kt` - Gyroscope sensor manager
- `MagnetometerManager.kt` - Magnetometer sensor manager
- Additional managers for other sensors

### Repository Module
Provides abstraction layer between ViewModels and data sources.

**Key Classes:**
- `SensorRepository.kt` - Main repository for sensor data

### ViewModel Module
Manages UI state and business logic.

**Key Classes:**
- `AccelerometerViewModel.kt` - Accelerometer screen state management
- Additional ViewModels for other screens

### UI Module
Jetpack Compose screens and components.

**Structure:**
```
ui/
├── screens/          # Full screen composables
├── components/       # Reusable UI components
├── theme/           # Material Design theme
└── navigation/      # Navigation setup
```

## Sensor Integration

### Adding a New Sensor

1. **Create Data Model**
```kotlin
data class NewSensorData(
    override val timestamp: Long = System.currentTimeMillis(),
    override val sensorType: SensorType = SensorType.NEW_SENSOR,
    val value: Float = 0f
) : SensorData()
```

2. **Create Sensor Manager**
```kotlin
class NewSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(...)
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_...)
    
    fun isAvailable(): Boolean = sensor != null
    
    fun getSensorFlow(): Flow<NewSensorData> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Process and emit data
                trySend(NewSensorData(value = event.values[0]))
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
```

3. **Add to Repository**
```kotlin
fun getNewSensorFlow(): Flow<NewSensorData> = 
    newSensorManager.getSensorFlow()
```

4. **Create ViewModel**
```kotlin
@HiltViewModel
class NewSensorViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewSensorUiState())
    val uiState: StateFlow<NewSensorUiState> = _uiState.asStateFlow()
    
    fun startMonitoring() {
        viewModelScope.launch {
            repository.getNewSensorFlow().collect { data ->
                _uiState.value = _uiState.value.copy(currentData = data)
            }
        }
    }
}
```

5. **Create UI Screen**
```kotlin
@Composable
fun NewSensorScreen(
    viewModel: NewSensorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // UI implementation
        }
    }
}
```

6. **Add to Navigation**
```kotlin
composable(Screen.NewSensor.route) {
    NewSensorScreen()
}
```

## Database Design

### Schema

```sql
CREATE TABLE sensor_readings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    sensorType TEXT NOT NULL,
    valueX REAL,
    valueY REAL,
    valueZ REAL,
    valueExtra REAL,
    accuracy REAL
);
```

### Best Practices

1. **Batch Inserts** - Use `insertReadings()` for multiple readings
2. **Cleanup Old Data** - Regularly call `deleteOldReadings(timestamp)`
3. **Limit Queries** - Use `LIMIT` clause to prevent memory issues
4. **Indexing** - Add indexes for frequently queried columns

## UI Components

### Reusable Components

#### SensorCard
Displays sensor value with animation:
```kotlin
SensorCard(
    label = "X-Axis",
    value = data.x,
    unit = "m/s²",
    color = Color.Red
)
```

#### SensorInfoDialog
Shows detailed sensor information:
```kotlin
SensorInfoDialog(
    sensorInfo = sensorInfo,
    onDismiss = { }
)
```

### Custom Visualizations

Use Canvas for custom sensor visualizations:
```kotlin
Canvas(modifier = Modifier.fillMaxSize()) {
    // Draw custom graphics
    drawLine(...)
    drawCircle(...)
}
```

## Testing Strategy

### Unit Tests

Test ViewModels in isolation:
```kotlin
@Test
fun `startMonitoring updates state correctly`() = runTest {
    // Given
    val testData = AccelerometerData(...)
    every { repository.getAccelerometerFlow() } returns flowOf(testData)
    
    // When
    viewModel.startMonitoring()
    
    // Then
    assertEquals(testData, viewModel.uiState.value.currentData)
}
```

### Instrumentation Tests

Test UI components:
```kotlin
@Test
fun accelerometerScreenDisplaysData() {
    composeTestRule.setContent {
        AccelerometerScreen()
    }
    
    composeTestRule.onNodeWithText("Accelerometer").assertExists()
}
```

## Best Practices

### 1. Sensor Management
- Always check sensor availability before use
- Unregister listeners in `onCleared()` or `awaitClose()`
- Use appropriate sampling rates (UI delay for UI updates)

### 2. State Management
- Use `StateFlow` for UI state
- Make state updates immutable (copy)
- Handle errors gracefully

### 3. Performance
- Limit data history size (e.g., last 100 readings)
- Use database queries efficiently
- Optimize Canvas drawing operations

### 4. Memory Management
- Clean up resources in ViewModel
- Use `viewModelScope` for coroutines
- Avoid memory leaks with sensor listeners

### 5. UI/UX
- Show loading states
- Display error messages clearly
- Provide sensor availability feedback
- Use animations judiciously

### 6. Code Quality
- Follow Kotlin coding conventions
- Write comprehensive KDoc comments
- Use meaningful variable names
- Keep functions small and focused

## Common Issues

### Issue: Sensor data not updating
**Solution:** Check if sensor listener is registered and not unregistered prematurely.

### Issue: App crashes on sensor access
**Solution:** Always check sensor availability before accessing.

### Issue: High battery consumption
**Solution:** Use appropriate sampling rates and unregister listeners when not needed.

### Issue: Database queries slow
**Solution:** Add indexes and limit query results.

## Resources

- [Android Sensors Overview](https://developer.android.com/guide/topics/sensors/sensors_overview)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## Version History

- **1.0.0-alpha** - Initial implementation with basic sensors
- Upcoming features in roadmap

---

Last Updated: February 2026
