package com.lusa.fluidwallpaper.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lusa.fluidwallpaper.model.Preset
import com.lusa.fluidwallpaper.utils.ColorPreferences

class FluidViewModel(application: Application) : AndroidViewModel(application) {
    
    // Effect parameters - Only Particle Flow (effect type 0)
    private val _effectType = MutableLiveData<Int>(0)
    val effectType: LiveData<Int> = _effectType
    
    private val _speed = MutableLiveData<Float>(1.0f)
    val speed: LiveData<Float> = _speed
    
    private val _viscosity = MutableLiveData<Float>(1.0f)
    val viscosity: LiveData<Float> = _viscosity
    
    private val _turbulence = MutableLiveData<Float>(0.5f)
    val turbulence: LiveData<Float> = _turbulence
    
    // Colors (RGB values 0-1)
    private val _color1 = MutableLiveData<FloatArray>(floatArrayOf(0.2f, 0.6f, 1.0f))
    val color1: LiveData<FloatArray> = _color1
    
    private val _color2 = MutableLiveData<FloatArray>(floatArrayOf(0.8f, 0.2f, 0.9f))
    val color2: LiveData<FloatArray> = _color2
    
    // Settings
    private val _batterySaveMode = MutableLiveData<Boolean>(false)
    val batterySaveMode: LiveData<Boolean> = _batterySaveMode
    
    private val _touchInteraction = MutableLiveData<Boolean>(true)
    val touchInteraction: LiveData<Boolean> = _touchInteraction
    
    private val _gyroscopeEnabled = MutableLiveData<Boolean>(false)
    val gyroscopeEnabled: LiveData<Boolean> = _gyroscopeEnabled
    
    private val _fpsLimit = MutableLiveData<Int>(60)
    val fpsLimit: LiveData<Int> = _fpsLimit
    
    // Presets
    private val _presets = MutableLiveData<List<Preset>>(emptyList())
    val presets: LiveData<List<Preset>> = _presets
    
    // Touch interaction
    private val _touchPosition = MutableLiveData<Pair<Float, Float>>()
    val touchPosition: LiveData<Pair<Float, Float>> = _touchPosition
    
    // Gyroscope data
    private val _gyroscopeData = MutableLiveData<Triple<Float, Float, Float>>()
    val gyroscopeData: LiveData<Triple<Float, Float, Float>> = _gyroscopeData
    
    fun setEffectType(type: Int) {
        // Only allow Particle Flow (effect type 0)
        _effectType.value = 0
    }
    
    fun setSpeed(speed: Float) {
        _speed.value = speed.coerceIn(0.1f, 5.0f)
    }
    
    fun setViscosity(viscosity: Float) {
        _viscosity.value = viscosity.coerceIn(0.1f, 3.0f)
    }
    
    fun setTurbulence(turbulence: Float) {
        _turbulence.value = turbulence.coerceIn(0.0f, 1.0f)
    }
    
    fun setColor1(color: FloatArray) {
        Log.d("FluidViewModel", "setColor1 called: r=${color[0]}, g=${color[1]}, b=${color[2]}")
        _color1.value = color.clone()
        // Save to SharedPreferences for wallpaper service
        val color2 = _color2.value ?: floatArrayOf(0.8f, 0.2f, 0.9f) // Default color2
        Log.d("FluidViewModel", "setColor1 - saving to SharedPreferences: color1=[${color[0]}, ${color[1]}, ${color[2]}], color2=[${color2[0]}, ${color2[1]}, ${color2[2]}]")
        ColorPreferences.saveColors(getApplication(), color.clone(), color2)
    }
    
    fun setColor2(color: FloatArray) {
        Log.d("FluidViewModel", "setColor2 called: r=${color[0]}, g=${color[1]}, b=${color[2]}")
        _color2.value = color.clone()
        // Save to SharedPreferences for wallpaper service
        val color1 = _color1.value ?: floatArrayOf(0.2f, 0.6f, 1.0f) // Default color1
        Log.d("FluidViewModel", "setColor2 - saving to SharedPreferences: color1=[${color1[0]}, ${color1[1]}, ${color1[2]}], color2=[${color[0]}, ${color[1]}, ${color[2]}]")
        ColorPreferences.saveColors(getApplication(), color1, color.clone())
    }
    
    fun setBatterySaveMode(enabled: Boolean) {
        _batterySaveMode.value = enabled
    }
    
    fun setTouchInteraction(enabled: Boolean) {
        _touchInteraction.value = enabled
    }
    
    fun setGyroscopeEnabled(enabled: Boolean) {
        _gyroscopeEnabled.value = enabled
    }
    
    fun setFpsLimit(fps: Int) {
        _fpsLimit.value = fps.coerceIn(15, 120)
    }
    
    fun setTouchPosition(x: Float, y: Float) {
        _touchPosition.value = Pair(x, y)
    }
    
    fun setGyroscopeData(x: Float, y: Float, z: Float) {
        _gyroscopeData.value = Triple(x, y, z)
    }
    
    fun savePreset(name: String) {
        val currentPreset = Preset(
            name = name,
            effectType = _effectType.value ?: 0,
            speed = _speed.value ?: 1.0f,
            viscosity = _viscosity.value ?: 1.0f,
            turbulence = _turbulence.value ?: 0.5f,
            color1 = _color1.value?.clone() ?: floatArrayOf(0.2f, 0.6f, 1.0f),
            color2 = _color2.value?.clone() ?: floatArrayOf(0.8f, 0.2f, 0.9f)
        )
        
        val currentPresets = _presets.value?.toMutableList() ?: mutableListOf()
        currentPresets.add(currentPreset)
        _presets.value = currentPresets
    }
    
    fun loadPreset(preset: Preset) {
        _effectType.value = preset.effectType
        _speed.value = preset.speed
        _viscosity.value = preset.viscosity
        _turbulence.value = preset.turbulence
        _color1.value = preset.color1.clone()
        _color2.value = preset.color2.clone()
    }
    
    fun deletePreset(preset: Preset) {
        val currentPresets = _presets.value?.toMutableList() ?: mutableListOf()
        currentPresets.remove(preset)
        _presets.value = currentPresets
    }
    
    fun getCurrentPreset(): Preset {
        return Preset(
            name = "Current",
            effectType = 0, // Always Particle Flow
            speed = _speed.value ?: 1.0f,
            viscosity = _viscosity.value ?: 1.0f,
            turbulence = _turbulence.value ?: 0.5f,
            color1 = _color1.value?.clone() ?: floatArrayOf(0.2f, 0.6f, 1.0f),
            color2 = _color2.value?.clone() ?: floatArrayOf(0.8f, 0.2f, 0.9f)
        )
    }
}
