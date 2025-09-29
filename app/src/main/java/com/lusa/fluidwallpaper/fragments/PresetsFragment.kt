package com.lusa.fluidwallpaper.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.lusa.fluidwallpaper.R
import com.lusa.fluidwallpaper.adapter.PresetsAdapter
import com.lusa.fluidwallpaper.databinding.FragmentPresetsBinding
import com.lusa.fluidwallpaper.model.Preset
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class PresetsFragment : Fragment() {
    
    private var _binding: FragmentPresetsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FluidViewModel
    private lateinit var presetsAdapter: PresetsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPresetsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[FluidViewModel::class.java]
        
        setupRecyclerView()
        setupButtons()
        setupPreview()
    }
    
    private fun setupRecyclerView() {
        presetsAdapter = PresetsAdapter(
            onPresetClick = { preset ->
                viewModel.loadPreset(preset)
                showPresetLoadedMessage(preset.name)
            },
            onPresetDelete = { preset ->
                showDeleteConfirmDialog(preset)
            },
            onPresetShare = { preset ->
                sharePreset(preset)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = presetsAdapter
        }
        
        // Observe presets changes
        viewModel.presets.observe(viewLifecycleOwner) { presets ->
            presetsAdapter.submitList(presets)
            binding.emptyView.visibility = if (presets.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupButtons() {
        binding.savePresetButton.setOnClickListener {
            showSavePresetDialog()
        }
        
        binding.importPresetButton.setOnClickListener {
            // Import preset from file
            importPreset()
        }
    }
    
    private fun setupPreview() {
        // Observe ViewModel changes and update preview
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
    
    private fun showSavePresetDialog() {
        val input = TextInputEditText(requireContext()).apply {
            hint = "Tên mẫu"
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Lưu mẫu")
            .setMessage("Nhập tên cho mẫu hiện tại:")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val name = input.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    viewModel.savePreset(name)
                    showPresetSavedMessage(name)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun showDeleteConfirmDialog(preset: Preset) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa mẫu")
            .setMessage("Bạn có chắc chắn muốn xóa mẫu \"${preset.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deletePreset(preset)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun sharePreset(preset: Preset) {
        val presetJson = presetToJson(preset)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, presetJson)
            putExtra(Intent.EXTRA_SUBJECT, "Fluid Wallpaper Preset: ${preset.name}")
        }
        startActivity(Intent.createChooser(intent, "Chia sẻ mẫu"))
    }
    
    private fun importPreset() {
        // This would typically involve file picker
        // For now, we'll show a placeholder
        android.widget.Toast.makeText(
            context,
            "Tính năng import sẽ được thêm trong phiên bản sau",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun presetToJson(preset: Preset): String {
        return """
        {
            "name": "${preset.name}",
            "effectType": ${preset.effectType},
            "speed": ${preset.speed},
            "viscosity": ${preset.viscosity},
            "turbulence": ${preset.turbulence},
            "color1": [${preset.color1.joinToString(",")}],
            "color2": [${preset.color2.joinToString(",")}]
        }
        """.trimIndent()
    }
    
    private fun showPresetSavedMessage(name: String) {
        android.widget.Toast.makeText(
            context,
            "Đã lưu mẫu \"$name\"",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun showPresetLoadedMessage(name: String) {
        android.widget.Toast.makeText(
            context,
            "Đã tải mẫu \"$name\"",
            android.widget.Toast.LENGTH_SHORT
        ).show()
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
