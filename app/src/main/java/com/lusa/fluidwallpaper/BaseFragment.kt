package com.lusa.fluidwallpaper

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewBinding>(private val bindingFactory: (LayoutInflater) -> T) : Fragment() {

    protected lateinit var binding: T
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = bindingFactory(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindComponent()
        bindEvent()
        bindData()
    }

    abstract fun bindComponent()
    abstract fun bindData()
    abstract fun bindEvent()

    fun initializeViewsAnimation(views: List<View>) {

        views.forEach { view ->
            view.alpha = 0f
            view.translationX = 0f
        }

        views.forEachIndexed { index, view ->
            val isOdd = (index + 1) % 2 == 1
            val delay = index * 50L

            val screenWidth = resources.displayMetrics.widthPixels.toFloat()
            val initialTranslationX = if (isOdd) {
                -screenWidth
            } else {
                screenWidth
            }

            view.translationX = initialTranslationX

            val animatorSet = AnimatorSet()
            val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            val translationAnimator = ObjectAnimator.ofFloat(view, "translationX", initialTranslationX, 0f)

            alphaAnimator.duration = 400
            translationAnimator.duration = 400

            animatorSet.playTogether(alphaAnimator, translationAnimator)
            animatorSet.startDelay = delay
            animatorSet.start()
        }
    }
}