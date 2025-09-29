package com.lusa.fluidwallpaper.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.lusa.fluidwallpaper.BaseFragment
import com.lusa.fluidwallpaper.databinding.FragmentTwoBinding
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel
import com.lusa.fluidwallpaper.utils.ColorPreferences
import com.skydoves.colorpickerview.listeners.ColorListener

class TwoFragment : BaseFragment<FragmentTwoBinding>(FragmentTwoBinding::inflate) {
    private lateinit var viewModel: FluidViewModel


    override fun bindComponent() {
        viewModel = ViewModelProvider(requireActivity(), AndroidViewModelFactory.getInstance(requireActivity().application))[FluidViewModel::class.java]

        setupColorPickers()
        setupPreview()
        setupGradientToggle()
    }

    override fun bindData() {

    }

    override fun bindEvent() {

    }

    private fun setupColorPickers() {
        // Configure ColorPickerView 1
        binding.colorPicker1.apply {
            setColorListener(object : ColorListener {
                override fun onColorSelected(color: Int, fromUser: Boolean) {
                    Log.d("TwoFragment", "ColorPicker1 - onColorSelected: color=$color, fromUser=$fromUser")
                    if (fromUser) {
                        val rgb = floatArrayOf(
                            Color.red(color) / 255f,
                            Color.green(color) / 255f,
                            Color.blue(color) / 255f
                        )
                        Log.d("TwoFragment", "ColorPicker1 - Setting color1: r=${rgb[0]}, g=${rgb[1]}, b=${rgb[2]}")
                        viewModel.setColor1(rgb)
                        // Force immediate update to wallpaper service
                        forceWallpaperServiceUpdate()
                    }
                }
            })
        }
        
        // Configure ColorPickerView 2
        binding.colorPicker2.apply {
            setColorListener(object : ColorListener {
                override fun onColorSelected(color: Int, fromUser: Boolean) {
                    Log.d("TwoFragment", "ColorPicker2 - onColorSelected: color=$color, fromUser=$fromUser")
                    if (fromUser) {
                        val rgb = floatArrayOf(
                            Color.red(color) / 255f,
                            Color.green(color) / 255f,
                            Color.blue(color) / 255f
                        )
                        Log.d("TwoFragment", "ColorPicker2 - Setting color2: r=${rgb[0]}, g=${rgb[1]}, b=${rgb[2]}")
                        viewModel.setColor2(rgb)
                        // Force immediate update to wallpaper service
                        forceWallpaperServiceUpdate()
                    }
                }
            })
        }
        
        // Set initial colors
        val color1 = viewModel.color1.value
        if (color1 != null) {
            val color = Color.rgb(
                (color1[0] * 255).toInt(),
                (color1[1] * 255).toInt(),
                (color1[2] * 255).toInt()
            )
            binding.colorPicker1.setInitialColor(color)
        }
        
        val color2 = viewModel.color2.value
        if (color2 != null) {
            val color = Color.rgb(
                (color2[0] * 255).toInt(),
                (color2[1] * 255).toInt(),
                (color2[2] * 255).toInt()
            )
            binding.colorPicker2.setInitialColor(color)
        }
    }
    
    private fun setupPreview() {
        // Observe color changes and update preview
        viewModel.color1.observe(viewLifecycleOwner) { color1 ->
            viewModel.color2.value?.let { color2 ->
                binding.previewView.setColors(color1, color2)
            }
            // Don't update ColorPickerView from observer to avoid conflicts
        }
        
        viewModel.color2.observe(viewLifecycleOwner) { color2 ->
            viewModel.color1.value?.let { color1 ->
                binding.previewView.setColors(color1, color2)
            }
            // Don't update ColorPickerView from observer to avoid conflicts
        }
        
        // Observe other parameters
        viewModel.effectType.observe(viewLifecycleOwner) { effectType ->
            binding.previewView.setEffectType(effectType)
        }
        
        viewModel.speed.observe(viewLifecycleOwner) { speed ->
            binding.previewView.setSpeed(speed)
        }
        
        viewModel.viscosity.observe(viewLifecycleOwner) { viscosity ->
            binding.previewView.setViscosity(viscosity)
        }
        
        viewModel.turbulence.observe(viewLifecycleOwner) { turbulence ->
            binding.previewView.setTurbulence(turbulence)
        }
    }
    
    private fun setupGradientToggle() {
        binding.gradientToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable gradient mode - show both color pickers
                binding.colorPicker2.visibility = View.VISIBLE
                binding.color2Label.visibility = View.VISIBLE
            } else {
                // Single color mode - hide second color picker
                binding.colorPicker2.visibility = View.GONE
                binding.color2Label.visibility = View.GONE
                
                // Set color2 same as color1
                viewModel.color1.value?.let { color1 ->
                    viewModel.setColor2(color1.clone())
                    // Update color picker 2 to show the same color
                    val color = Color.rgb(
                        (color1[0] * 255).toInt(),
                        (color1[1] * 255).toInt(),
                        (color1[2] * 255).toInt()
                    )
                    binding.colorPicker2.setInitialColor(color)
                }
            }
        }
        
        // Set initial state
        binding.gradientToggle.isChecked = true
    }
    
    private fun forceWallpaperServiceUpdate() {
        // Force wallpaper service to check for color updates immediately
        // This is done by updating the timestamp in SharedPreferences
        val color1 = viewModel.color1.value ?: floatArrayOf(0.2f, 0.6f, 1.0f)
        val color2 = viewModel.color2.value ?: floatArrayOf(0.8f, 0.2f, 0.9f)
        Log.d("TwoFragment", "forceWallpaperServiceUpdate - Saving colors: color1=[${color1[0]}, ${color1[1]}, ${color1[2]}], color2=[${color2[0]}, ${color2[1]}, ${color2[2]}]")
        ColorPreferences.saveColors(requireContext(), color1, color2)
        
        // Force immediate reload by sending a broadcast or using a different approach
        // For now, we'll rely on the timestamp checking mechanism
        Log.d("TwoFragment", "forceWallpaperServiceUpdate - Colors saved, wallpaper service should detect change within 500ms")
    }
    
    override fun onResume() {
        super.onResume()
        binding.previewView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.previewView.pause()
    }
}
