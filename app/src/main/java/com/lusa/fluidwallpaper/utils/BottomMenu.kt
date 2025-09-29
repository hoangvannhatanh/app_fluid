package com.lusa.fluidwallpaper.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.lusa.fluidwallpaper.R
import com.lusa.fluidwallpaper.extensions.animateSelection
import com.lusa.fluidwallpaper.databinding.LayoutBottomMenuBinding
import com.lusa.fluidwallpaper.extensions.invisible
import com.lusa.fluidwallpaper.extensions.show

class BottomMenu : ConstraintLayout {
    var onMenuClick: OnMenuClick? = null
    private lateinit var context: Context
    private lateinit var binding: LayoutBottomMenuBinding

    constructor(context: Context) : super(context) {
        this.context = context
        setupClickListeners()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        binding = LayoutBottomMenuBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnOne.setOnClickListener {
            animateSelection(listOf(binding.btnOne), {
                selectScreen(ScreenTag.ONE)
                onMenuClick?.onClick(Action.OPEN_ONE)
            })
        }
        binding.btnTwo.setOnClickListener {
            animateSelection(listOf(binding.btnTwo), {
                selectScreen(ScreenTag.TWO)
                onMenuClick?.onClick(Action.OPEN_TWO)
            })
        }
        binding.btnThree.setOnClickListener {
            animateSelection(listOf(binding.btnThree), {
                selectScreen(ScreenTag.THREE)
                onMenuClick?.onClick(Action.OPEN_THREE)
            })
        }
        binding.btnFour.setOnClickListener {
            animateSelection(listOf(binding.btnFour), {
                selectScreen(ScreenTag.FOUR)
                onMenuClick?.onClick(Action.OPEN_FOUR)
            })
        }
    }

    fun selectScreen(view: ScreenTag) {
        when (view) {
            ScreenTag.ONE -> {
                binding.icOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_effects_ena))
                binding.icTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_colors))
                binding.icThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_settings))
                binding.icFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_presets))


                binding.tvOne.setTextColor(ContextCompat.getColor(context, R.color.color_FF8FA8))
                binding.tvTwo.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvThree.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvFour.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))

                binding.ivDivider1.show()
                binding.ivDivider2.invisible()
                binding.ivDivider3.invisible()
                binding.ivDivider4.invisible()
            }
            ScreenTag.TWO -> {
                binding.icOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_effects))
                binding.icTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_colors_ena))
                binding.icThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_settings))
                binding.icFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_presets))

                binding.tvOne.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvTwo.setTextColor(ContextCompat.getColor(context, R.color.color_FF8FA8))
                binding.tvThree.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvFour.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))

                binding.ivDivider1.invisible()
                binding.ivDivider2.show()
                binding.ivDivider3.invisible()
                binding.ivDivider4.invisible()
            }
            ScreenTag.THREE -> {
                binding.icOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_effects))
                binding.icTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_colors))
                binding.icThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_settings_ena))
                binding.icFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_presets))

                binding.tvOne.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvTwo.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvThree.setTextColor(ContextCompat.getColor(context, R.color.color_FF8FA8))
                binding.tvFour.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))

                binding.ivDivider1.invisible()
                binding.ivDivider2.invisible()
                binding.ivDivider3.show()
                binding.ivDivider4.invisible()
            }
            ScreenTag.FOUR -> {
                binding.icOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_effects))
                binding.icTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_colors))
                binding.icThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_settings))
                binding.icFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_presets_ena))

                binding.tvOne.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvTwo.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvThree.setTextColor(ContextCompat.getColor(context, R.color.color_B4A9D2))
                binding.tvFour.setTextColor(ContextCompat.getColor(context, R.color.color_FF8FA8))

                binding.ivDivider1.invisible()
                binding.ivDivider2.invisible()
                binding.ivDivider3.invisible()
                binding.ivDivider4.show()
            }
        }
    }

    fun addListener(onMenuClickObject: OnMenuClick?) {
        onMenuClick = onMenuClickObject
    }

    enum class Action {
        OPEN_ONE, OPEN_TWO, OPEN_THREE, OPEN_FOUR
    }

    enum class ScreenTag {
        ONE, TWO, THREE, FOUR
    }

    interface OnMenuClick {
        fun onClick(action: Action?)
    }
}