package com.lusa.fluidwallpaper.utils

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.lusa.fluidwallpaper.service.SuperSimpleWallpaperService

object WallpaperUtils {
    
    private const val TAG = "WallpaperUtils"

    fun setBothScreenWallpaper(context: Context): Boolean {
        return try {
            val serviceComponent = ComponentName(context, SuperSimpleWallpaperService::class.java)
            Log.d(TAG, "Setting wallpaper for both screens with service: ${serviceComponent.className}")
            
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                serviceComponent
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
