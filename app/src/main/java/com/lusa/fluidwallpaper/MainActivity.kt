package com.lusa.fluidwallpaper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lusa.fluidwallpaper.sensor.SensorIntegration
import com.lusa.fluidwallpaper.viewmodel.FluidViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var navController: NavController
    private lateinit var viewModel: FluidViewModel
    private lateinit var sensorIntegration: SensorIntegration
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViewModel()
        setupNavigation()
        setupSensors()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[FluidViewModel::class.java]
    }
    
    private fun setupSensors() {
        sensorIntegration = SensorIntegration(this, viewModel)
        lifecycle.addObserver(sensorIntegration)
        
        // Observe gyroscope setting changes
        viewModel.gyroscopeEnabled.observe(this) { enabled ->
            sensorIntegration.setGyroscopeEnabled(enabled)
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavView.setupWithNavController(navController)
    }
}
