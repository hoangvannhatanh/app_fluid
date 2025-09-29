package com.lusa.fluidwallpaper.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lusa.fluidwallpaper.fragments.FourFragment
import com.lusa.fluidwallpaper.fragments.OneFragment
import com.lusa.fluidwallpaper.fragments.ThreeFragment
import com.lusa.fluidwallpaper.fragments.TwoFragment

class HomeViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val oneFragment: OneFragment by lazy { OneFragment() }
    private val twoFragment: TwoFragment by lazy { TwoFragment() }
    private val threeFragment: ThreeFragment by lazy { ThreeFragment() }
    private val fourFragment: FourFragment by lazy { FourFragment() }

    @SuppressLint("NotifyDataSetChanged")
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> oneFragment
            1 -> twoFragment
            2 -> threeFragment
            else -> fourFragment
        }
    }

    override fun getItemCount(): Int = 4
}
