package com.lusa.fluidwallpaper.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.os.PowerManager
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.os.Build
import android.provider.Settings
import kotlin.random.Random
import com.lusa.fluidwallpaper.model.Particle
import com.lusa.fluidwallpaper.utils.ColorPreferences

class SuperSimpleWallpaperService : WallpaperService() {
    
    override fun onCreateEngine(): Engine {
        return SuperSimpleEngine()
    }
    
    private inner class SuperSimpleEngine : Engine() {
        private var surfaceHolder: SurfaceHolder? = null
        private var isVisible = false
        private var renderingThread: Thread? = null
        private var shouldRender = false
        
        // Main particles (completely isolated)
        private val particles = mutableListOf<Particle>()
        private val maxParticles = 50
        private var time = 0f
        
        // Touch effects (completely separate - no interference)
        private val touchEffects = mutableListOf<Particle>()
        private var isTouching = false
        private var touchX = 0f
        private var touchY = 0f
        
        // Colors from SharedPreferences
        private var color1 = floatArrayOf(0.2f, 0.6f, 1.0f)
        private var color2 = floatArrayOf(0.8f, 0.2f, 0.9f)
        private var lastColorUpdateTime = 0L
        private var lastKnownUpdateTime = 0L
        
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            this.surfaceHolder = surfaceHolder
            setTouchEventsEnabled(true) // ENABLE TOUCH FOR VISUAL EFFECTS
            
            // Load colors from SharedPreferences
            loadColors()
            
            // Samsung-specific initialization
            Log.d("SuperSimpleWallpaper", "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            Log.d("SuperSimpleWallpaper", "Android Version: ${Build.VERSION.RELEASE}")
            Log.d("SuperSimpleWallpaper", "Touch events enabled: ${isPreview()}")
            
            initializeParticles()
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                // Reload colors when becoming visible (in case they were changed)
                Log.d("SuperSimpleWallpaper", "onVisibilityChanged - becoming visible, reloading colors")
                loadColors()
                refreshParticles() // Update existing particles with new colors
                startRendering()
            } else {
                stopRendering()
            }
        }
        
        override fun onDestroy() {
            super.onDestroy()
            stopRendering()
        }
        
        override fun onTouchEvent(event: MotionEvent?) {
            if (event == null) return

            // Enhanced touch handling for Samsung lock screen
            var x = 0f
            var y = 0f
            val surfaceFrame = surfaceHolder?.surfaceFrame
            if (surfaceFrame != null) {
                x = event.x / surfaceFrame.width().toFloat()
                y = event.y / surfaceFrame.height().toFloat()
            } else {
                // Samsung-specific fallback for lock screen
                try {
                    val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = windowManager.defaultDisplay
                    val size = android.graphics.Point()
                    display.getSize(size)
                    x = event.x / size.x.toFloat()
                    y = event.y / size.y.toFloat()
                } catch (e: Exception) {
                    // Ultimate fallback for Samsung devices
                    x = event.x / 1440f // Samsung S22 Ultra resolution
                    y = event.y / 3088f
                }
            }
            
            // Clamp coordinates to valid range
            x = x.coerceIn(0f, 1f)
            y = y.coerceIn(0f, 1f)
            
            // Samsung-specific logging
            val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"
            Log.d("SuperSimpleWallpaper", "Touch event on $deviceInfo: action=${event.action}, rawX=${event.x}, rawY=${event.y}, normalizedX=$x, normalizedY=$y, isTouching=$isTouching")
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouching = true
                    touchX = x
                    touchY = y
                    
                    Log.d("SuperSimpleWallpaper", "Samsung Touch DOWN at ($x, $y) - Lock screen interaction")
                    
                    // Create simple touch effect - just a burst
                    repeat(8) {
                        val angle = (it * 45f) * kotlin.math.PI / 180f
                        val speed = 0.02f + Random.nextFloat() * 0.01f
                        val vx = kotlin.math.cos(angle).toFloat() * speed
                        val vy = kotlin.math.sin(angle).toFloat() * speed
                        
                        touchEffects.add(Particle(
                            x = x,
                            y = y,
                            vx = vx,
                            vy = vy,
                            color = Color.WHITE,
                            size = Random.nextFloat() * 10f + 5f,
                            life = 1.0f
                        ))
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    isTouching = true
                    touchX = x
                    touchY = y
                    
                    // Add trail effect (less frequent)
                    if (Random.nextFloat() < 0.3f) {
                        touchEffects.add(Particle(
                            x = x,
                            y = y,
                            vx = 0f,
                            vy = 0f,
                            color = Color.parseColor("#80FFFFFF"), // Semi-transparent
                            size = Random.nextFloat() * 8f + 3f,
                            life = 0.5f
                        ))
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isTouching = false
                    
                    Log.d("SuperSimpleWallpaper", "Samsung Touch UP/CANCEL at ($x, $y) - Lock screen interaction")
                    
                    // Create release effect
                    repeat(4) {
                        val angle = (it * 90f) * kotlin.math.PI / 180f
                        val speed = 0.015f + Random.nextFloat() * 0.005f
                        val vx = kotlin.math.cos(angle).toFloat() * speed
                        val vy = kotlin.math.sin(angle).toFloat() * speed
                        
                        touchEffects.add(Particle(
                            x = x,
                            y = y,
                            vx = vx,
                            vy = vy,
                            color = Color.parseColor("#FF00FFFF"), // Cyan
                            size = Random.nextFloat() * 12f + 8f,
                            life = 0.8f
                        ))
                    }
                }
            }
        }
        
        private fun initializeParticles() {
            particles.clear()
            repeat(maxParticles) {
                particles.add(createParticle())
            }
        }
        
        private fun refreshParticles() {
            // Update existing particles with new colors
            particles.forEach { particle ->
                particle.color = getRandomColor()
            }
        }
        
        private fun forceColorUpdate() {
            // Force reload colors and refresh all particles
            loadColors()
            refreshParticles()
        }
        
        private fun checkForColorUpdates() {
            // Check if colors have been updated by checking timestamp
            val currentUpdateTime = ColorPreferences.getLastUpdateTime(this@SuperSimpleWallpaperService)
            
            Log.d("SuperSimpleWallpaper", "checkForColorUpdates - currentUpdateTime=$currentUpdateTime, lastKnownUpdateTime=$lastKnownUpdateTime")
            
            if (currentUpdateTime > lastKnownUpdateTime) {
                Log.d("SuperSimpleWallpaper", "Colors updated, reloading...")
                loadColors()
                refreshParticles()
            }
        }
        
        private fun createParticle(): Particle {
            val angle = Random.nextFloat() * 2 * kotlin.math.PI
            val speed = 0.04f // Slightly faster for better visibility
            return Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = kotlin.math.cos(angle).toFloat() * speed,
                vy = kotlin.math.sin(angle).toFloat() * speed,
                color = getRandomColor(),
                size = Random.nextFloat() * 20f + 15f,
                life = 1.0f
            )
        }
        
        private fun loadColors() {
            color1 = ColorPreferences.getColor1(this@SuperSimpleWallpaperService)
            color2 = ColorPreferences.getColor2(this@SuperSimpleWallpaperService)
            lastKnownUpdateTime = ColorPreferences.getLastUpdateTime(this@SuperSimpleWallpaperService)
            Log.d("SuperSimpleWallpaper", "loadColors - color1=[${color1[0]}, ${color1[1]}, ${color1[2]}], color2=[${color2[0]}, ${color2[1]}, ${color2[2]}], timestamp=$lastKnownUpdateTime")
        }
        
        private fun getRandomColor(): Int {
            // Use the colors from SharedPreferences instead of fixed colors
            val colorArray = if (Random.nextBoolean()) color1 else color2
            return Color.rgb(
                (colorArray[0] * 255).toInt(),
                (colorArray[1] * 255).toInt(),
                (colorArray[2] * 255).toInt()
            )
        }
        
        private fun startRendering() {
            shouldRender = true
            renderingThread = Thread {
                while (shouldRender && isVisible) {
                    renderFrame()
                    try {
                        Thread.sleep(16) // ~60 FPS
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
            renderingThread?.start()
        }
        
        private fun stopRendering() {
            shouldRender = false
            renderingThread?.interrupt()
            try {
                renderingThread?.join()
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
        
        private fun renderFrame() {
            val canvas = surfaceHolder?.lockCanvas()
            if (canvas != null) {
                try {
                    canvas.drawColor(Color.BLACK)
                    
                    // Check for color updates every 100ms for more responsive updates
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastColorUpdateTime > 100) {
                        checkForColorUpdates()
                        lastColorUpdateTime = currentTime
                    }
                    
                    // Update main particles (COMPLETELY ISOLATED)
                    updateParticles()
                    drawParticles(canvas)
                    
                    // Update touch effects (SEPARATE SYSTEM)
                    updateTouchEffects()
                    drawTouchEffects(canvas)
                    
                    time += 0.016f
                } finally {
                    surfaceHolder?.unlockCanvasAndPost(canvas)
                }
            }
        }
        
        private fun updateParticles() {
            // Create copy to avoid ConcurrentModificationException
            val particlesToUpdate = particles.toList()
            
            particlesToUpdate.forEach { particle ->
                if (isTouching) {
                    // TOUCH MODE: Move particles toward touch point and orbit
                    val dx = touchX - particle.x
                    val dy = touchY - particle.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    
                    if (distance > 0.001f) {
                        // Move toward touch point
                        val attractionForce = 0.02f
                        val normalizedDx = dx / distance
                        val normalizedDy = dy / distance
                        
                        particle.vx += normalizedDx * attractionForce
                        particle.vy += normalizedDy * attractionForce
                        
                        // Add orbital motion (tangential to the circle)
                        val orbitalSpeed = 0.015f
                        val tangentX = -normalizedDy * orbitalSpeed
                        val tangentY = normalizedDx * orbitalSpeed
                        
                        particle.vx += tangentX
                        particle.vy += tangentY
                        
                        // Limit speed
                        val currentSpeed = kotlin.math.sqrt(particle.vx * particle.vx + particle.vy * particle.vy)
                        val maxSpeed = 0.05f
                        if (currentSpeed > maxSpeed) {
                            particle.vx = (particle.vx / currentSpeed) * maxSpeed
                            particle.vy = (particle.vy / currentSpeed) * maxSpeed
                        }
                    }
                } else {
                    // NORMAL MODE: Return to normal movement
                    val currentSpeed = kotlin.math.sqrt(particle.vx * particle.vx + particle.vy * particle.vy)
                    val targetSpeed = 0.04f
                    
                    // Gradually return to normal speed
                    if (kotlin.math.abs(currentSpeed - targetSpeed) > 0.001f) {
                        val angle = Random.nextFloat() * 2 * kotlin.math.PI
                        particle.vx = kotlin.math.cos(angle).toFloat() * targetSpeed
                        particle.vy = kotlin.math.sin(angle).toFloat() * targetSpeed
                    }
                    
                    // Safety check - if velocity is zero, force movement
                    if (particle.vx == 0f && particle.vy == 0f) {
                        val angle = Random.nextFloat() * 2 * kotlin.math.PI
                        particle.vx = kotlin.math.cos(angle).toFloat() * targetSpeed
                        particle.vy = kotlin.math.sin(angle).toFloat() * targetSpeed
                    }
                }
                
                // Move particle
                particle.x += particle.vx
                particle.y += particle.vy
                
                // Wrap around screen
                if (particle.x < 0f) particle.x = 1f
                if (particle.x > 1f) particle.x = 0f
                if (particle.y < 0f) particle.y = 1f
                if (particle.y > 1f) particle.y = 0f
            }
        }
        
        private fun updateTouchEffects() {
            // Remove dead touch effects
            touchEffects.removeAll { effect ->
                (effect.life ?: 0f) <= 0f
            }
            
            // Limit touch effects to prevent performance issues
            if (touchEffects.size > 30) {
                val toRemove = touchEffects.size - 30
                repeat(toRemove) {
                    if (touchEffects.isNotEmpty()) {
                        touchEffects.removeAt(0)
                    }
                }
            }
            
            // Update touch effects - create copy to avoid ConcurrentModificationException
            val effectsToUpdate = touchEffects.toList()
            
            effectsToUpdate.forEach { effect ->
                effect.x += effect.vx
                effect.y += effect.vy
                effect.life = ((effect.life ?: 0f) - 0.02f).coerceAtLeast(0f)
                
                // Wrap around screen
                if (effect.x < 0f) effect.x = 1f
                if (effect.x > 1f) effect.x = 0f
                if (effect.y < 0f) effect.y = 1f
                if (effect.y > 1f) effect.y = 0f
            }
        }
        
        private fun drawTouchEffects(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            // Create a copy to avoid ConcurrentModificationException
            val effectsToDraw = touchEffects.toList()
            
            effectsToDraw.forEach { effect ->
                val x = effect.x * width
                val y = effect.y * height
                val radius = (effect.size ?: 0f) * (effect.life ?: 0f)
                
                paint.color = effect.color
                paint.alpha = ((effect.life ?: 0f) * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(x, y, radius, paint)
                
                // Glow effect
                paint.alpha = ((effect.life ?: 0f) * 80).toInt().coerceIn(0, 80)
                canvas.drawCircle(x, y, radius * 2f, paint)
            }
        }
        
        private fun drawParticles(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            // Debug info
            paint.color = Color.WHITE
            paint.textSize = 24f
            canvas.drawText("ORBITAL TOUCH INTERACTION", 50f, 40f, paint)
            canvas.drawText("Device: ${Build.MANUFACTURER} ${Build.MODEL}", 50f, 70f, paint)
            canvas.drawText("Main Particles: ${particles.size}/50", 50f, 100f, paint)
            canvas.drawText("Touch Effects: ${touchEffects.size}/30", 50f, 130f, paint)
            canvas.drawText("Touching: $isTouching", 50f, 160f, paint)
            
            // Show particle speeds
            val avgSpeed = particles.take(5).map { 
                kotlin.math.sqrt(it.vx * it.vx + it.vy * it.vy) 
            }.average()
            canvas.drawText("Avg Speed: ${String.format("%.3f", avgSpeed)}", 50f, 190f, paint)
            canvas.drawText("Time: ${time.toInt()}s", 50f, 225f, paint)
            
            // Lock screen info
            val surfaceFrame = surfaceHolder?.surfaceFrame
            if (surfaceFrame != null) {
                paint.color = Color.parseColor("#FF00FF00") // Green - Surface available
                canvas.drawText("SURFACE: AVAILABLE", 50f, 220f, paint)
                canvas.drawText("Size: ${surfaceFrame.width()}x${surfaceFrame.height()}", 50f, 250f, paint)
            } else {
                paint.color = Color.parseColor("#FFFF0000") // Red - No surface
                canvas.drawText("SURFACE: LOCK SCREEN", 50f, 220f, paint)
                canvas.drawText("Samsung Lock Screen Mode", 50f, 250f, paint)
                
                // Try to get screen dimensions
                try {
                    val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = windowManager.defaultDisplay
                    val size = android.graphics.Point()
                    display.getSize(size)
                    canvas.drawText("Screen: ${size.x}x${size.y}", 50f, 280f, paint)
                    canvas.drawText("Using Samsung fallback", 50f, 310f, paint)
                } catch (e: Exception) {
                    canvas.drawText("Screen: Unknown", 50f, 280f, paint)
                    canvas.drawText("Using S22 Ultra fallback", 50f, 310f, paint)
                }
            }
            
            // Mode status
            val modeY = if (surfaceFrame == null) 365f else 330f
            if (isTouching) {
                paint.color = Color.parseColor("#FFFFFF00") // Yellow - Orbital mode
                canvas.drawText("MODE: ORBITAL TOUCH", 50f, modeY, paint)
                canvas.drawText("Touch: (${String.format("%.2f", touchX)}, ${String.format("%.2f", touchY)})", 50f, modeY + 35f, paint)
            } else {
                paint.color = Color.parseColor("#FF00FF00") // Green - Normal mode
                canvas.drawText("MODE: NORMAL MOVEMENT", 50f, modeY, paint)
                canvas.drawText("Particles returning to normal", 50f, modeY + 35f, paint)
            }
            
            // Performance status
            val statusY = if (surfaceFrame == null) 435f else 400f
            if (avgSpeed > 0.001f) {
                paint.color = Color.parseColor("#FF00FF00") // Green - OK
                canvas.drawText("STATUS: ACTIVE", 50f, statusY, paint)
            } else {
                paint.color = Color.parseColor("#FFFF0000") // Red - Problem
                canvas.drawText("STATUS: STUCK", 50f, statusY, paint)
            }
            
            // Draw particles - create copy to avoid ConcurrentModificationException
            val particlesToDraw = particles.toList()
            
            particlesToDraw.forEach { particle ->
                val x = particle.x * width
                val y = particle.y * height
                val radius = (particle.size ?: 0f) * (particle.life ?: 0f)
                
                paint.color = particle.color
                paint.alpha = 255
                canvas.drawCircle(x, y, radius, paint)
                
                // Glow effect
                paint.alpha = 100
                canvas.drawCircle(x, y, radius * 2f, paint)
            }
        }
    }
}
