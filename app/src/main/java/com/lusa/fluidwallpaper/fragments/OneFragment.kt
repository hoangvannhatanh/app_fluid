package com.lusa.fluidwallpaper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lusa.fluidwallpaper.BaseFragment
import com.lusa.fluidwallpaper.R
import com.lusa.fluidwallpaper.adapter.EffectsAdapter
import com.lusa.fluidwallpaper.databinding.FragmentOneBinding
import com.lusa.fluidwallpaper.model.Effect
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class OneFragment : BaseFragment<FragmentOneBinding>(FragmentOneBinding::inflate) {

    private lateinit var viewModel: FluidViewModel
    private lateinit var effectsAdapter: EffectsAdapter
    
    private val effects = listOf(
        Effect(0, "Particle Flow", "Hiệu ứng dòng hạt", R.drawable.ic_effects)
    )

    override fun bindComponent() {
        viewModel = ViewModelProvider(requireActivity())[FluidViewModel::class.java]

        setupRecyclerView()
        setupPreview()
    }

    override fun bindData() {

    }

    override fun bindEvent() {

    }
    
    private fun setupRecyclerView() {
        effectsAdapter = EffectsAdapter(effects) { effect ->
            viewModel.setEffectType(effect.id)
            binding.previewView.setEffectType(effect.id)
        }
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = effectsAdapter
        }
    }
    
    private fun setupPreview() {
        // Set initial effect
        binding.previewView.setEffectType(0)
        
        // Observe ViewModel changes
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
    
    override fun onResume() {
        super.onResume()
        binding.previewView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.previewView.pause()
    }
}
