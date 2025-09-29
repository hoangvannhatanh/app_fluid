package com.lusa.fluidwallpaper.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.lusa.fluidwallpaper.R
import com.lusa.fluidwallpaper.utils.WallpaperUtils

class LockScreenHelpDialog : DialogFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_lock_screen_help, container, false)
        
        val instructionsText = view.findViewById<TextView>(R.id.instructions_text)
        val deviceInfoText = view.findViewById<TextView>(R.id.device_info_text)
        
        // Set device-specific instructions
        val instructions = WallpaperUtils.getLockScreenInstructions()
        instructionsText.text = instructions
        
        // Show device info
        val deviceInfo = "Thiết bị: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
                "Hỗ trợ màn hình khóa riêng: ${if (WallpaperUtils.supportsLockScreenWallpaper()) "Có" else "Không"}"
        deviceInfoText.text = deviceInfo
        
        return view
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Hướng dẫn đặt hình nền màn hình khóa")
        return dialog
    }
    
    companion object {
        fun newInstance(): LockScreenHelpDialog {
            return LockScreenHelpDialog()
        }
    }
}
