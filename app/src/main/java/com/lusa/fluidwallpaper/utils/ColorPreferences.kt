package com.lusa.fluidwallpaper.utils

import android.content.Context
import android.content.SharedPreferences

object ColorPreferences {
    private const val PREFS_NAME = "fluid_wallpaper_colors"
    private const val KEY_COLOR1_R = "color1_r"
    private const val KEY_COLOR1_G = "color1_g"
    private const val KEY_COLOR1_B = "color1_b"
    private const val KEY_COLOR2_R = "color2_r"
    private const val KEY_COLOR2_G = "color2_g"
    private const val KEY_COLOR2_B = "color2_b"
    private const val KEY_LAST_UPDATE = "last_color_update"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveColors(context: Context, color1: FloatArray, color2: FloatArray) {
        val prefs = getSharedPreferences(context)
        val timestamp = System.currentTimeMillis()
        prefs.edit().apply {
            putFloat(KEY_COLOR1_R, color1[0])
            putFloat(KEY_COLOR1_G, color1[1])
            putFloat(KEY_COLOR1_B, color1[2])
            putFloat(KEY_COLOR2_R, color2[0])
            putFloat(KEY_COLOR2_G, color2[1])
            putFloat(KEY_COLOR2_B, color2[2])
            putLong(KEY_LAST_UPDATE, timestamp)
            apply()
        }
        android.util.Log.d("ColorPreferences", "saveColors - color1=[${color1[0]}, ${color1[1]}, ${color1[2]}], color2=[${color2[0]}, ${color2[1]}, ${color2[2]}], timestamp=$timestamp")
    }
    
    fun getColor1(context: Context): FloatArray {
        val prefs = getSharedPreferences(context)
        val color = floatArrayOf(
            prefs.getFloat(KEY_COLOR1_R, 0.2f),
            prefs.getFloat(KEY_COLOR1_G, 0.6f),
            prefs.getFloat(KEY_COLOR1_B, 1.0f)
        )
        android.util.Log.d("ColorPreferences", "getColor1 - color=[${color[0]}, ${color[1]}, ${color[2]}]")
        return color
    }
    
    fun getColor2(context: Context): FloatArray {
        val prefs = getSharedPreferences(context)
        val color = floatArrayOf(
            prefs.getFloat(KEY_COLOR2_R, 0.8f),
            prefs.getFloat(KEY_COLOR2_G, 0.2f),
            prefs.getFloat(KEY_COLOR2_B, 0.9f)
        )
        android.util.Log.d("ColorPreferences", "getColor2 - color=[${color[0]}, ${color[1]}, ${color[2]}]")
        return color
    }
    
    fun getLastUpdateTime(context: Context): Long {
        val prefs = getSharedPreferences(context)
        return prefs.getLong(KEY_LAST_UPDATE, 0L)
    }
}
