# SensorHub - Educational Mobile Sensor Platform

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![MinSDK](https://img.shields.io/badge/MinSDK-26-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 📱 Overview

SensorHub is a comprehensive educational Android application designed to demonstrate and teach mobile sensor integration, user interaction patterns, UI/UX design, and affective computing. Built with modern Android development practices, it serves as a practical learning platform for students studying mobile development.

## ✨ Features

### 🎯 Sensor Integration
- **Accelerometer** - Measure device acceleration and movement
- **Gyroscope** - Track device rotation and orientation
- **Magnetometer** - Detect magnetic field strength and compass heading
- **Light Sensor** - Measure ambient light levels
- **GPS** - Location tracking and geolocation
- **Proximity Sensor** - Detect nearby objects
- **Barometer** - Atmospheric pressure measurement

### 🎨 User Interactions
- **Gesture Recognition** - Touch, swipe, pinch, rotate
- **Voice Recognition** - Speech-to-text capabilities
- **Haptic Feedback** - Vibration patterns and tactile responses

### 💡 Affective Computing
- **Emotion Analysis** - Analyze user emotional state from sensor data
- **Behavior Patterns** - Track and visualize user interaction patterns
- **Real-time Processing** - Live data analysis and visualization

### 🎯 Technical Features
- **Material Design 3** - Modern, adaptive UI
- **Jetpack Compose** - Declarative UI framework
- **MVVM Architecture** - Clean, testable code structure
- **Room Database** - Local data persistence
- **Hilt** - Dependency injection
- **Coroutines & Flow** - Reactive data streams

## 🏗️ Architecture

The app follows the MVVM (Model-View-ViewModel) architecture pattern:

```
app/
├── data/
│   ├── model/          # Data classes
│   ├── repository/     # Data repositories
│   └── database/       # Room database
├── sensors/            # Sensor managers
├── viewmodel/          # ViewModels
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   ├── theme/          # Material Design theme
│   └── navigation/     # Navigation setup
├── utils/              # Utility classes
├── affective/          # Affective computing module
└── di/                 # Dependency injection
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 26+
- Physical Android device (recommended for sensor testing)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/sensorhub.git
cd sensorhub
```

2. Open the project in Android Studio

3. Sync Gradle dependencies:
```
File > Sync Project with Gradle Files
```

4. Run the app:
```
Run > Run 'app'
```

## 📚 Project Structure

### Key Components

#### Sensors
Each sensor has a dedicated manager class that provides:
- Availability checking
- Real-time data streaming via Flow
- Sensor information retrieval

Example:
```kotlin
class AccelerometerManager(context: Context) {
    fun isAvailable(): Boolean
    fun getAccelerometerFlow(): Flow<AccelerometerData>
    fun getSensorInfo(): SensorInfo?
}
```

#### ViewModels
ViewModels manage UI state and business logic:
```kotlin
@HiltViewModel
class AccelerometerViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    val uiState: StateFlow<AccelerometerUiState>
    fun startMonitoring()
    fun stopMonitoring()
}
```

#### UI Screens
Compose-based screens with Material Design 3:
```kotlin
@Composable
fun AccelerometerScreen(
    viewModel: AccelerometerViewModel = hiltViewModel()
) {
    // UI implementation
}
```

## 🧪 Testing

### Unit Tests
Run unit tests:
```bash
./gradlew test
```

### Instrumentation Tests
Run UI tests:
```bash
./gradlew connectedAndroidTest
```

## 📖 Educational Use

This project is designed for educational purposes and includes:

### Learning Objectives
- Understanding Android sensor APIs
- Implementing MVVM architecture
- Working with Jetpack Compose
- Reactive programming with Flow
- Database operations with Room
- Dependency injection with Hilt
- Material Design 3 implementation

### Suggested Exercises
1. Add a new sensor (temperature, humidity)
2. Implement custom visualizations
3. Create gesture-based games
4. Build affective computing models
5. Add data export functionality
6. Implement unit tests for ViewModels

## 🔧 Technologies Used

- **Kotlin** - Programming language
- **Jetpack Compose** - UI framework
- **Material Design 3** - Design system
- **Hilt** - Dependency injection
- **Room** - Local database
- **Coroutines & Flow** - Asynchronous programming
- **Navigation Compose** - Navigation
- **ViewModel** - State management
- **LiveData** - Observable data
- **WorkManager** - Background tasks

## 📝 Code Examples

### Collecting Sensor Data
```kotlin
viewModelScope.launch {
    repository.getAccelerometerFlow()
        .collect { data ->
            _uiState.value = _uiState.value.copy(
                currentData = data
            )
        }
}
```

### Database Operations
```kotlin
suspend fun saveSensorReading(sensorData: SensorData) {
    val reading = sensorData.toSensorReading()
    sensorDao.insertReading(reading)
}
```

### Compose UI
```kotlin
@Composable
fun SensorCard(
    label: String,
    value: Float,
    unit: String,
    color: Color
) {
    Card {
        Text("$label: $value $unit")
    }
}
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- Educational Project - For learning purposes

## 🙏 Acknowledgments

- Android Developers Documentation
- Material Design Guidelines
- Jetpack Compose Documentation
- Open source community

## 📞 Support

For questions or support:
- Open an issue in the repository
- Contact: support@sensorhub.edu
- Documentation: docs.sensorhub.edu

## 🗺️ Roadmap

### Phase 1 (Current)
- [x] Basic sensor integration
- [x] MVVM architecture
- [x] Jetpack Compose UI
- [ ] Complete all sensor implementations

### Phase 2
- [ ] Gesture recognition system
- [ ] Voice commands
- [ ] Haptic feedback patterns
- [ ] Advanced visualizations

### Phase 3
- [ ] Affective computing module
- [ ] Machine learning integration
- [ ] Data export/import
- [ ] Cloud synchronization

### Phase 4
- [ ] Comprehensive testing
- [ ] Performance optimization
- [ ] Accessibility improvements
- [ ] Documentation completion

## 📊 Project Status

🚧 **In Development** - Active development in progress

Current Version: 1.0.0-alpha
Last Updated: February 2026

---

**Built with ❤️ for education and learning**
