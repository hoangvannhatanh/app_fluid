package com.lusa.fluidwallpaper.fragments

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lusa.fluidwallpaper.PreviewActivity
import com.lusa.fluidwallpaper.R
import com.lusa.fluidwallpaper.databinding.FragmentSettingsBinding
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel
import com.lusa.fluidwallpaper.utils.WallpaperUtils
import com.lusa.fluidwallpaper.dialogs.LockScreenHelpDialog

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FluidViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[FluidViewModel::class.java]
        
        setupControls()
        setupPreview()
        setupButtons()
    }
    
    private fun setupControls() {
        // Speed control
        binding.speedSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setSpeed(value / 100f)
            }
        }
        
        // Viscosity control
        binding.viscositySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setViscosity(value / 100f)
            }
        }
        
        // Turbulence control
        binding.turbulenceSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setTurbulence(value / 100f)
            }
        }
        
        // Battery save mode
        binding.batterySaveSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setBatterySaveMode(isChecked)
        }
        
        // Touch interaction
        binding.touchInteractionSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTouchInteraction(isChecked)
        }
        
        // Gyroscope
        binding.gyroscopeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setGyroscopeEnabled(isChecked)
        }
        
        // FPS limit
        binding.fpsSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setFpsLimit(value.toInt())
            }
        }
    }
    
    private fun setupPreview() {
        // Observe ViewModel changes and update preview
        viewModel.speed.observe(viewLifecycleOwner) { speed ->
            binding.speedSlider.value = speed * 100f
            binding.previewView.setSpeed(speed)
        }
        
        viewModel.viscosity.observe(viewLifecycleOwner) { viscosity ->
            binding.viscositySlider.value = viscosity * 100f
            binding.previewView.setViscosity(viscosity)
        }
        
        viewModel.turbulence.observe(viewLifecycleOwner) { turbulence ->
            binding.turbulenceSlider.value = turbulence * 100f
            binding.previewView.setTurbulence(turbulence)
        }
        
        viewModel.batterySaveMode.observe(viewLifecycleOwner) { enabled ->
            binding.batterySaveSwitch.isChecked = enabled
            binding.previewView.setBatterySaveMode(enabled)
        }
        
        viewModel.touchInteraction.observe(viewLifecycleOwner) { enabled ->
            binding.touchInteractionSwitch.isChecked = enabled
        }
        
        viewModel.gyroscopeEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.gyroscopeSwitch.isChecked = enabled
        }
        
        viewModel.fpsLimit.observe(viewLifecycleOwner) { fps ->
            binding.fpsSlider.value = fps.toFloat()
        }
        
        // Other parameters
        viewModel.effectType.observe(viewLifecycleOwner) { effectType ->
            binding.previewView.setEffectType(effectType)
        }
        
        viewModel.color1.observe(viewLifecycleOwner) { color1 ->
            viewModel.color2.value?.let { color2 ->
                binding.previewView.setColors(color1, color2)
            }
        }
        
        viewModel.color2.observe(viewLifecycleOwner) { color2 ->
            viewModel.color1.value?.let { color1 ->
                binding.previewView.setColors(color1, color2)
            }
        }
    }
    
    private fun setupButtons() {
        // Preview button
        binding.previewButton.setOnClickListener {
            val intent = Intent(context, PreviewActivity::class.java)
            startActivity(intent)
        }
        
        // Set wallpaper button (legacy - now uses both screens)
        binding.setWallpaperButton.setOnClickListener {
            setWallpaper()
        }
        
        // New wallpaper buttons
        binding.setHomeWallpaperButton.setOnClickListener {
            setHomeScreenWallpaper()
        }
        
        binding.setLockWallpaperButton.setOnClickListener {
            setLockScreenWallpaper()
        }
        
        binding.setBothWallpaperButton.setOnClickListener {
            setBothScreenWallpaper()
        }
        
        // Help button
        binding.helpButton.setOnClickListener {
            showLockScreenHelp()
        }
        
        // Touch interaction setup
        binding.previewView.setTouchCallback { x, y ->
            if (viewModel.touchInteraction.value == true) {
                viewModel.setTouchPosition(x, y)
            }
        }
        
        // Ensure touch interaction is enabled by default
        if (viewModel.touchInteraction.value != true) {
            viewModel.setTouchInteraction(true)
        }
    }
    
    private fun setWallpaper() {
        try {
            // Save current settings first
            saveCurrentSettings()
            
            // Use the new WallpaperUtils for better lock screen support
            WallpaperUtils.setBothScreenWallpaper(requireContext())
            
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Lỗi: ${e.message}. Vui lòng thử cách khác.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun setHomeScreenWallpaper() {
        try {
            saveCurrentSettings()
            WallpaperUtils.setHomeScreenWallpaper(requireContext())
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Lỗi: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun setLockScreenWallpaper() {
        try {
            saveCurrentSettings()
            
            if (WallpaperUtils.supportsLockScreenWallpaper()) {
                WallpaperUtils.setLockScreenWallpaper(requireContext())
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Thiết bị không hỗ trợ đặt hình nền riêng cho màn hình khóa. ${WallpaperUtils.getLockScreenInstructions()}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Lỗi: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun setBothScreenWallpaper() {
        try {
            saveCurrentSettings()
            WallpaperUtils.setBothScreenWallpaper(requireContext())
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Lỗi: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showLockScreenHelp() {
        val dialog = LockScreenHelpDialog.newInstance()
        dialog.show(parentFragmentManager, "LockScreenHelpDialog")
    }
    
    private fun saveCurrentSettings() {
        val preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        val currentPreset = viewModel.getCurrentPreset()
        
        preferences.edit().apply {
            putInt("effect_type", currentPreset.effectType)
            putFloat("speed", currentPreset.speed)
            putFloat("viscosity", currentPreset.viscosity)
            putFloat("turbulence", currentPreset.turbulence)
            putFloat("color1_r", currentPreset.color1[0])
            putFloat("color1_g", currentPreset.color1[1])
            putFloat("color1_b", currentPreset.color1[2])
            putFloat("color2_r", currentPreset.color2[0])
            putFloat("color2_g", currentPreset.color2[1])
            putFloat("color2_b", currentPreset.color2[2])
            putBoolean("battery_save", viewModel.batterySaveMode.value ?: false)
            putBoolean("touch_interaction", viewModel.touchInteraction.value ?: true)
            putBoolean("gyroscope_enabled", viewModel.gyroscopeEnabled.value ?: false)
            putInt("fps_limit", viewModel.fpsLimit.value ?: 60)
            apply()
        }
    }
    
    override fun onResume() {
        super.onResume()
        binding.previewView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.previewView.pause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
