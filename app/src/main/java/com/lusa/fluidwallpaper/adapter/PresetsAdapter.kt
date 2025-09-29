package com.lusa.fluidwallpaper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lusa.fluidwallpaper.databinding.ItemPresetBinding
import com.lusa.fluidwallpaper.model.Preset

class PresetsAdapter(
    private val onPresetClick: (Preset) -> Unit,
    private val onPresetDelete: (Preset) -> Unit,
    private val onPresetShare: (Preset) -> Unit
) : ListAdapter<Preset, PresetsAdapter.PresetViewHolder>(PresetDiffCallback()) {
    
    class PresetViewHolder(private val binding: ItemPresetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            preset: Preset,
            onPresetClick: (Preset) -> Unit,
            onPresetDelete: (Preset) -> Unit,
            onPresetShare: (Preset) -> Unit
        ) {
            binding.preset = preset
            
            binding.root.setOnClickListener { onPresetClick(preset) }
            binding.deleteButton.setOnClickListener { onPresetDelete(preset) }
            binding.shareButton.setOnClickListener { onPresetShare(preset) }
            
            binding.executePendingBindings()
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val binding = ItemPresetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PresetViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        holder.bind(getItem(position), onPresetClick, onPresetDelete, onPresetShare)
    }
}

class PresetDiffCallback : DiffUtil.ItemCallback<Preset>() {
    override fun areItemsTheSame(oldItem: Preset, newItem: Preset): Boolean {
        return oldItem.name == newItem.name
    }
    
    override fun areContentsTheSame(oldItem: Preset, newItem: Preset): Boolean {
        return oldItem == newItem
    }
}
