package com.lusa.fluidwallpaper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lusa.fluidwallpaper.databinding.ItemEffectBinding
import com.lusa.fluidwallpaper.model.Effect

class EffectsAdapter(
    private val effects: List<Effect>,
    private val onEffectClick: (Effect) -> Unit
) : RecyclerView.Adapter<EffectsAdapter.EffectViewHolder>() {
    
    class EffectViewHolder(private val binding: ItemEffectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(effect: Effect, onEffectClick: (Effect) -> Unit) {
            binding.effect = effect
            binding.root.setOnClickListener { onEffectClick(effect) }
            binding.executePendingBindings()
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val binding = ItemEffectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EffectViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        holder.bind(effects[position], onEffectClick)
    }
    
    override fun getItemCount() = effects.size
}
