package com.example.sensorhub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for SensorHub
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class SensorHubApplication : Application()
