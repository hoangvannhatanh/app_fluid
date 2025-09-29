package com.lusa.fluidwallpaper.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lusa.fluidwallpaper.databinding.FragmentColorsBinding
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class ColorsFragment : Fragment() {
    
    private var _binding: FragmentColorsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FluidViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[FluidViewModel::class.java]
        
        setupColorPickers()
        setupPreview()
        setupGradientToggle()
    }
    
    private fun setupColorPickers() {
        // Color 1 picker
        binding.colorPicker1.setOnColorSelectedListener { color ->
            val rgb = floatArrayOf(
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
            )
            viewModel.setColor1(rgb)
        }
        
        // Color 2 picker
        binding.colorPicker2.setOnColorSelectedListener { color ->
            val rgb = floatArrayOf(
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
            )
            viewModel.setColor2(rgb)
        }
        
        // Set initial colors
        val color1 = viewModel.color1.value
        if (color1 != null) {
            val color = Color.rgb(
                (color1[0] * 255).toInt(),
                (color1[1] * 255).toInt(),
                (color1[2] * 255).toInt()
            )
            binding.colorPicker1.setColor(color)
        }
        
        val color2 = viewModel.color2.value
        if (color2 != null) {
            val color = Color.rgb(
                (color2[0] * 255).toInt(),
                (color2[1] * 255).toInt(),
                (color2[2] * 255).toInt()
            )
            binding.colorPicker2.setColor(color)
        }
    }
    
    private fun setupPreview() {
        // Observe color changes and update preview
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
                }
            }
        }
        
        // Set initial state
        binding.gradientToggle.isChecked = true
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
