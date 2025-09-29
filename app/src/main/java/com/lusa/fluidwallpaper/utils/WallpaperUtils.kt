package com.lusa.fluidwallpaper.utils

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.lusa.fluidwallpaper.service.SuperSimpleWallpaperService

object WallpaperUtils {
    
    private const val TAG = "WallpaperUtils"
    
    /**
     * Set live wallpaper for home screen only
     */
    fun setHomeScreenWallpaper(context: Context): Boolean {
        return try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, SuperSimpleWallpaperService::class.java)
            )
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Toast.makeText(context, "Đang mở wallpaper picker cho màn hình chính...", Toast.LENGTH_SHORT).show()
                true
            } else {
                // Fallback method
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                context.startActivity(fallbackIntent)
                Toast.makeText(context, "Vui lòng chọn 'Fluid Wallpaper' từ danh sách", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting home screen wallpaper", e)
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    
    /**
     * Set live wallpaper for lock screen only (Android 7.1+)
     */
    fun setLockScreenWallpaper(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                // For Android 7.1+ we can use the lock screen specific intent
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, SuperSimpleWallpaperService::class.java)
                )
                intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", 
                    ComponentName(context, SuperSimpleWallpaperService::class.java))
                
                // Try to set for lock screen specifically
                intent.putExtra("android.service.wallpaper.extra.LOCK_SCREEN", true)
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    Toast.makeText(context, "Đang mở wallpaper picker cho màn hình khóa...", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    // Fallback: Open general wallpaper picker
                    openWallpaperPicker(context, "lock")
                    true
                }
            } else {
                // For older Android versions, use general method
                openWallpaperPicker(context, "lock")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting lock screen wallpaper", e)
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    
    /**
     * Set live wallpaper for both home and lock screen
     */
    fun setBothScreenWallpaper(context: Context): Boolean {
        return try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, SuperSimpleWallpaperService::class.java)
            )
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Toast.makeText(context, "Đang mở wallpaper picker cho cả hai màn hình...", Toast.LENGTH_SHORT).show()
                true
            } else {
                openWallpaperPicker(context, "both")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting both screen wallpaper", e)
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
    
    /**
     * Open wallpaper picker with specific target
     */
    private fun openWallpaperPicker(context: Context, target: String) {
        try {
            val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            context.startActivity(intent)
            
            val message = when (target) {
                "lock" -> "Vui lòng chọn 'Fluid Wallpaper' và chọn 'Chỉ màn hình khóa'"
                "home" -> "Vui lòng chọn 'Fluid Wallpaper' và chọn 'Chỉ màn hình chính'"
                "both" -> "Vui lòng chọn 'Fluid Wallpaper' và chọn 'Cả hai màn hình'"
                else -> "Vui lòng chọn 'Fluid Wallpaper' từ danh sách"
            }
            
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening wallpaper picker", e)
            Toast.makeText(context, "Không thể mở wallpaper picker: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Check if device supports separate lock screen wallpaper
     */
    fun supportsLockScreenWallpaper(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    }
    
    /**
     * Get device-specific instructions for setting lock screen wallpaper
     */
    fun getLockScreenInstructions(): String {
        return when {
            Build.MANUFACTURER.equals("samsung", ignoreCase = true) -> {
                "Samsung: Vào Cài đặt > Màn hình > Hình nền > Chọn 'Fluid Wallpaper' > Chọn 'Chỉ màn hình khóa'"
            }
            Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) -> {
                "Xiaomi: Vào Cài đặt > Màn hình > Hình nền > Chọn 'Fluid Wallpaper' > Chọn 'Màn hình khóa'"
            }
            Build.MANUFACTURER.equals("huawei", ignoreCase = true) -> {
                "Huawei: Vào Cài đặt > Màn hình > Hình nền > Chọn 'Fluid Wallpaper' > Chọn 'Màn hình khóa'"
            }
            else -> {
                "Vào Cài đặt > Màn hình > Hình nền > Chọn 'Fluid Wallpaper' > Chọn 'Chỉ màn hình khóa'"
            }
        }
    }
}
