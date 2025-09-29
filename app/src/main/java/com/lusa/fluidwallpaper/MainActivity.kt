package com.lusa.fluidwallpaper

import androidx.lifecycle.ViewModelProvider
import com.lusa.fluidwallpaper.adapter.HomeViewPagerAdapter
import com.lusa.fluidwallpaper.databinding.ActivityMainBinding
import com.lusa.fluidwallpaper.sensor.SensorIntegration
import com.lusa.fluidwallpaper.utils.BottomMenu
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private lateinit var viewModel: FluidViewModel
    private lateinit var sensorIntegration: SensorIntegration
    private lateinit var homeViewPagerAdapter: HomeViewPagerAdapter

    override fun bindComponent() {
        viewModel = ViewModelProvider(this)[FluidViewModel::class.java]

        initBottomView()

        setupSensors()
    }

    override fun bindData() {

    }

    override fun bindEvent() {
        binding.bottomMenu.addListener(
            object : BottomMenu.OnMenuClick {
                override fun onClick(action: BottomMenu.Action?) {
                    val newIndex = when (action) {
                        BottomMenu.Action.OPEN_ONE -> 0
                        BottomMenu.Action.OPEN_TWO -> 1
                        BottomMenu.Action.OPEN_THREE -> 2
                        BottomMenu.Action.OPEN_FOUR -> 3
                        else -> 0
                    }
                    if (binding.vpMain.currentItem != newIndex) {
                        binding.vpMain.setCurrentItem(newIndex,false)
                    }
                }
            })
    }

    private fun initBottomView() {
        homeViewPagerAdapter = HomeViewPagerAdapter(this)
        binding.vpMain.adapter = homeViewPagerAdapter
        binding.vpMain.isUserInputEnabled = false
    }
    
    private fun setupSensors() {
        sensorIntegration = SensorIntegration(this, viewModel)
        lifecycle.addObserver(sensorIntegration)
        
        viewModel.gyroscopeEnabled.observe(this) { enabled ->
            sensorIntegration.setGyroscopeEnabled(enabled)
        }
    }
}
