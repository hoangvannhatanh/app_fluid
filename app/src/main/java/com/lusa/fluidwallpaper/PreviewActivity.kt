package com.lusa.fluidwallpaper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.lusa.fluidwallpaper.databinding.ActivityPreviewBinding
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class PreviewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPreviewBinding
    private lateinit var viewModel: FluidViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this, AndroidViewModelFactory.getInstance(application))[FluidViewModel::class.java]
        
        setupPreview()
        setupControls()
    }
    
    private fun setupPreview() {
        // Observe ViewModel changes and update preview
        viewModel.effectType.observe(this) { effectType ->
            binding.previewView.setEffectType(effectType)
        }
        
        viewModel.speed.observe(this) { speed ->
            binding.previewView.setSpeed(speed)
        }
        
        viewModel.viscosity.observe(this) { viscosity ->
            binding.previewView.setViscosity(viscosity)
        }
        
        viewModel.turbulence.observe(this) { turbulence ->
            binding.previewView.setTurbulence(turbulence)
        }
        
        viewModel.color1.observe(this) { color1 ->
            viewModel.color2.value?.let { color2 ->
                binding.previewView.setColors(color1, color2)
            }
        }
        
        viewModel.color2.observe(this) { color2 ->
            viewModel.color1.value?.let { color1 ->
                binding.previewView.setColors(color1, color2)
            }
        }
        
        // Handle touch interaction
        binding.previewView.setTouchCallback { x, y ->
            viewModel.setTouchPosition(x, y)
        }
    }
    
    private fun setupControls() {
        binding.closeButton.setOnClickListener {
            finish()
        }
        
        binding.setWallpaperButton.setOnClickListener {
            setWallpaper()
        }
    }
    
    private fun setWallpaper() {
        try {
            // This would typically involve setting the wallpaper service
            android.widget.Toast.makeText(
                this,
                getString(R.string.wallpaper_set_success),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            finish()
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                getString(R.string.wallpaper_set_error),
                android.widget.Toast.LENGTH_SHORT
            ).show()
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
}
