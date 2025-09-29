package com.lusa.fluidwallpaper.sensor

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class SensorIntegration(
    private val context: Context,
    private val viewModel: FluidViewModel
) : LifecycleObserver {
    
    private val gyroscopeManager = GyroscopeManager(context)
    
    init {
        setupGyroscope()
    }
    
    private fun setupGyroscope() {
        gyroscopeManager.setOnGyroscopeDataListener { x, y, z ->
            viewModel.setGyroscopeData(x, y, z)
        }
        
        gyroscopeManager.setOnAccelerometerDataListener { x, y, z ->
            // Use accelerometer data for device orientation
            viewModel.setGyroscopeData(x, y, z)
        }
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startSensors() {
        if (viewModel.gyroscopeEnabled.value == true) {
            gyroscopeManager.startListening()
        }
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopSensors() {
        gyroscopeManager.stopListening()
    }
    
    fun isGyroscopeAvailable(): Boolean {
        return gyroscopeManager.isAvailable()
    }
    
    fun setGyroscopeEnabled(enabled: Boolean) {
        if (enabled && isGyroscopeAvailable()) {
            gyroscopeManager.startListening()
        } else {
            gyroscopeManager.stopListening()
        }
    }
}
