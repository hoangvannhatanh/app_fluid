package com.lusa.fluidwallpaper.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.*

class GyroscopeManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var onGyroscopeDataListener: ((Float, Float, Float) -> Unit)? = null
    private var onAccelerometerDataListener: ((Float, Float, Float) -> Unit)? = null
    
    private var isListening = false
    
    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)
    private val lastAccelerometerReading = FloatArray(3)
    
    fun setOnGyroscopeDataListener(listener: (Float, Float, Float) -> Unit) {
        onGyroscopeDataListener = listener
    }
    
    fun setOnAccelerometerDataListener(listener: (Float, Float, Float) -> Unit) {
        onAccelerometerDataListener = listener
    }
    
    fun startListening() {
        if (!isListening) {
            gyroscope?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
            }
            
            accelerometer?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
            }
            
            isListening = true
        }
    }
    
    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }
    
    fun isAvailable(): Boolean {
        return gyroscope != null || accelerometer != null
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]
                    
                    // Apply low-pass filter to reduce noise
                    val filteredX = lowPassFilter(x, lastAccelerometerReading[0])
                    val filteredY = lowPassFilter(y, lastAccelerometerReading[1])
                    val filteredZ = lowPassFilter(z, lastAccelerometerReading[2])
                    
                    onGyroscopeDataListener?.invoke(filteredX, filteredY, filteredZ)
                }
                
                Sensor.TYPE_ACCELEROMETER -> {
                    val alpha = 0.8f
                    
                    // Isolate the force of gravity with the low-pass filter
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0]
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1]
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2]
                    
                    // Remove the gravity contribution with the high-pass filter
                    linearAcceleration[0] = sensorEvent.values[0] - gravity[0]
                    linearAcceleration[1] = sensorEvent.values[1] - gravity[1]
                    linearAcceleration[2] = sensorEvent.values[2] - gravity[2]
                    
                    // Calculate device orientation
                    val rotationX = atan2(gravity[1], gravity[2]).toDegrees()
                    val rotationY = atan2(-gravity[0], sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2])).toDegrees()
                    val rotationZ = 0f // Not used for device orientation
                    
                    onAccelerometerDataListener?.invoke(rotationX, rotationY, rotationZ)
                    
                    // Store current reading for gyroscope filtering
                    lastAccelerometerReading[0] = linearAcceleration[0]
                    lastAccelerometerReading[1] = linearAcceleration[1]
                    lastAccelerometerReading[2] = linearAcceleration[2]
                }

                else -> {}
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    private fun lowPassFilter(input: Float, previousOutput: Float): Float {
        val alpha = 0.1f // Smoothing factor
        return alpha * input + (1 - alpha) * previousOutput
    }
    
    private fun Float.toDegrees(): Float {
        return this * 180f / PI.toFloat()
    }
}
