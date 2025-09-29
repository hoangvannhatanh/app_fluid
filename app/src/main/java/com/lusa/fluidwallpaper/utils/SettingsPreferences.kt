package com.lusa.fluidwallpaper.utils

import android.content.Context
import android.content.SharedPreferences

object SettingsPreferences {
    private const val PREFS_NAME = "fluid_wallpaper_settings"

    private const val KEY_EFFECT_TYPE = "effect_type"
    private const val KEY_SPEED = "speed"
    private const val KEY_VISCOSITY = "viscosity"
    private const val KEY_TURBULENCE = "turbulence"
    private const val KEY_LAST_UPDATE = "last_settings_update"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSettings(
        context: Context,
        effectType: Int,
        speed: Float,
        viscosity: Float,
        turbulence: Float
    ) {
        val timestamp = System.currentTimeMillis()
        prefs(context).edit().apply {
            putInt(KEY_EFFECT_TYPE, effectType)
            putFloat(KEY_SPEED, speed)
            putFloat(KEY_VISCOSITY, viscosity)
            putFloat(KEY_TURBULENCE, turbulence)
            putLong(KEY_LAST_UPDATE, timestamp)
            apply()
        }
    }

    fun getEffectType(context: Context): Int = prefs(context).getInt(KEY_EFFECT_TYPE, 0)
    fun getSpeed(context: Context): Float = prefs(context).getFloat(KEY_SPEED, 1.0f)
    fun getViscosity(context: Context): Float = prefs(context).getFloat(KEY_VISCOSITY, 1.0f)
    fun getTurbulence(context: Context): Float = prefs(context).getFloat(KEY_TURBULENCE, 0.5f)
    fun getLastUpdateTime(context: Context): Long = prefs(context).getLong(KEY_LAST_UPDATE, 0L)
}


