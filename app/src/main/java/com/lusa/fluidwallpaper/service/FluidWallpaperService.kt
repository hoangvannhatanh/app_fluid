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

class FluidWallpaperService : WallpaperService() {
    
    override fun onCreateEngine(): Engine {
        return FluidEngine()
    }
    
    private inner class FluidEngine : Engine() {
        private var surfaceHolder: SurfaceHolder? = null
        private var isVisible = false
        private var renderingThread: Thread? = null
        private var shouldRender = false
        
        // Main particles
        private val particles = mutableListOf<Particle>()
        private val maxParticles = 40
        private var time = 0f
        
        // Touch effects
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
            setTouchEventsEnabled(true)
            
            Log.d("FluidWallpaperService", "FluidEngine created - Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            
            // Load initial colors
            loadColors()
            initializeParticles()
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                Log.d("FluidWallpaperService", "onVisibilityChanged - becoming visible, reloading colors")
                loadColors()
                refreshParticles()
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

            var x = 0f
            var y = 0f
            val surfaceFrame = surfaceHolder?.surfaceFrame
            if (surfaceFrame != null) {
                x = event.x / surfaceFrame.width().toFloat()
                y = event.y / surfaceFrame.height().toFloat()
            } else {
                try {
                    val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = windowManager.defaultDisplay
                    val size = android.graphics.Point()
                    display.getSize(size)
                    x = event.x / size.x.toFloat()
                    y = event.y / size.y.toFloat()
                } catch (e: Exception) {
                    x = event.x / 1440f
                    y = event.y / 3088f
                }
            }
            
            x = x.coerceIn(0f, 1f)
            y = y.coerceIn(0f, 1f)
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouching = true
                    touchX = x
                    touchY = y
                    createTouchBurst(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    isTouching = true
                    touchX = x
                    touchY = y
                    createTouchTrail(x, y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isTouching = false
                    createTouchRelease(x, y)
                }
            }
        }
        
        private fun createTouchBurst(x: Float, y: Float) {
            repeat(6) {
                val angle = (it * 60f) * kotlin.math.PI / 180f
                val speed = 0.02f + Random.nextFloat() * 0.01f
                val vx = kotlin.math.cos(angle).toFloat() * speed
                val vy = kotlin.math.sin(angle).toFloat() * speed
                
                touchEffects.add(Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = Color.WHITE,
                    size = Random.nextFloat() * 8f + 4f,
                    life = 1.0f
                ))
            }
        }
        
        private fun createTouchTrail(x: Float, y: Float) {
            if (Random.nextFloat() < 0.2f) {
                touchEffects.add(Particle(
                    x = x,
                    y = y,
                    vx = 0f,
                    vy = 0f,
                    color = Color.parseColor("#60FFFFFF"),
                    size = Random.nextFloat() * 6f + 2f,
                    life = 0.3f
                ))
            }
        }
        
        private fun createTouchRelease(x: Float, y: Float) {
            repeat(3) {
                val angle = (it * 120f) * kotlin.math.PI / 180f
                val speed = 0.015f + Random.nextFloat() * 0.005f
                val vx = kotlin.math.cos(angle).toFloat() * speed
                val vy = kotlin.math.sin(angle).toFloat() * speed
                
                touchEffects.add(Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = Color.parseColor("#FF00FFFF"),
                    size = Random.nextFloat() * 10f + 6f,
                    life = 0.6f
                ))
            }
        }
        
        private fun initializeParticles() {
            particles.clear()
            repeat(maxParticles) {
                particles.add(createParticle())
            }
        }
        
        private fun createParticle(): Particle {
            val angle = Random.nextFloat() * 2 * kotlin.math.PI
            val speed = 0.03f
            return Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = kotlin.math.cos(angle).toFloat() * speed,
                vy = kotlin.math.sin(angle).toFloat() * speed,
                color = getRandomColor(),
                size = Random.nextFloat() * 15f + 10f,
                life = 1.0f
            )
        }
        
        private fun loadColors() {
            color1 = ColorPreferences.getColor1(this@FluidWallpaperService)
            color2 = ColorPreferences.getColor2(this@FluidWallpaperService)
            lastKnownUpdateTime = ColorPreferences.getLastUpdateTime(this@FluidWallpaperService)
            Log.d("FluidWallpaperService", "loadColors - color1=[${color1[0]}, ${color1[1]}, ${color1[2]}], color2=[${color2[0]}, ${color2[1]}, ${color2[2]}], timestamp=$lastKnownUpdateTime")
        }
        
        private fun getRandomColor(): Int {
            val colorArray = if (Random.nextBoolean()) color1 else color2
            return Color.rgb(
                (colorArray[0] * 255).toInt(),
                (colorArray[1] * 255).toInt(),
                (colorArray[2] * 255).toInt()
            )
        }
        
        private fun refreshParticles() {
            particles.forEach { particle ->
                particle.color = getRandomColor()
            }
        }
        
        private fun checkForColorUpdates() {
            val currentUpdateTime = ColorPreferences.getLastUpdateTime(this@FluidWallpaperService)
            
            Log.d("FluidWallpaperService", "checkForColorUpdates - currentUpdateTime=$currentUpdateTime, lastKnownUpdateTime=$lastKnownUpdateTime")
            
            if (currentUpdateTime > lastKnownUpdateTime) {
                Log.d("FluidWallpaperService", "Colors updated, reloading...")
                loadColors()
                refreshParticles()
            }
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
                    
                    // Check for color updates every 200ms
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastColorUpdateTime > 200) {
                        checkForColorUpdates()
                        lastColorUpdateTime = currentTime
                    }
                    
                    // Update and draw particles
                    updateParticles()
                    drawParticles(canvas)
                    
                    // Update and draw touch effects
                    updateTouchEffects()
                    drawTouchEffects(canvas)
                    
                    time += 0.016f
                } finally {
                    surfaceHolder?.unlockCanvasAndPost(canvas)
                }
            }
        }
        
        private fun updateParticles() {
            val particlesToUpdate = particles.toList()
            
            particlesToUpdate.forEach { particle ->
                if (isTouching) {
                    // Touch mode: attract particles to touch point
                    val dx = touchX - particle.x
                    val dy = touchY - particle.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    
                    if (distance > 0.001f) {
                        val attractionForce = 0.015f
                        val normalizedDx = dx / distance
                        val normalizedDy = dy / distance
                        
                        particle.vx += normalizedDx * attractionForce
                        particle.vy += normalizedDy * attractionForce
                        
                        // Add orbital motion
                        val orbitalSpeed = 0.01f
                        val tangentX = -normalizedDy * orbitalSpeed
                        val tangentY = normalizedDx * orbitalSpeed
                        
                        particle.vx += tangentX
                        particle.vy += tangentY
                        
                        // Limit speed
                        val currentSpeed = kotlin.math.sqrt(particle.vx * particle.vx + particle.vy * particle.vy)
                        val maxSpeed = 0.04f
                        if (currentSpeed > maxSpeed) {
                            particle.vx = (particle.vx / currentSpeed) * maxSpeed
                            particle.vy = (particle.vy / currentSpeed) * maxSpeed
                        }
                    }
                } else {
                    // Normal mode: return to normal movement
                    val currentSpeed = kotlin.math.sqrt(particle.vx * particle.vx + particle.vy * particle.vy)
                    val targetSpeed = 0.03f
                    
                    if (kotlin.math.abs(currentSpeed - targetSpeed) > 0.001f) {
                        val angle = Random.nextFloat() * 2 * kotlin.math.PI
                        particle.vx = kotlin.math.cos(angle).toFloat() * targetSpeed
                        particle.vy = kotlin.math.sin(angle).toFloat() * targetSpeed
                    }
                    
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
            touchEffects.removeAll { effect ->
                (effect.life ?: 0f) <= 0f
            }
            
            if (touchEffects.size > 25) {
                val toRemove = touchEffects.size - 25
                repeat(toRemove) {
                    if (touchEffects.isNotEmpty()) {
                        touchEffects.removeAt(0)
                    }
                }
            }
            
            val effectsToUpdate = touchEffects.toList()
            
            effectsToUpdate.forEach { effect ->
                effect.x += effect.vx
                effect.y += effect.vy
                effect.life = ((effect.life ?: 0f) - 0.015f).coerceAtLeast(0f)
                
                if (effect.x < 0f) effect.x = 1f
                if (effect.x > 1f) effect.x = 0f
                if (effect.y < 0f) effect.y = 1f
                if (effect.y > 1f) effect.y = 0f
            }
        }
        
        private fun drawParticles(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val particlesToDraw = particles.toList()
            
            particlesToDraw.forEach { particle ->
                val x = particle.x * width
                val y = particle.y * height
                val radius = (particle.size ?: 0f) * (particle.life ?: 0f)
                
                paint.color = particle.color
                paint.alpha = 255
                canvas.drawCircle(x, y, radius, paint)
                
                // Glow effect
                paint.alpha = 80
                canvas.drawCircle(x, y, radius * 1.5f, paint)
            }
        }
        
        private fun drawTouchEffects(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val effectsToDraw = touchEffects.toList()
            
            effectsToDraw.forEach { effect ->
                val x = effect.x * width
                val y = effect.y * height
                val radius = (effect.size ?: 0f) * (effect.life ?: 0f)
                
                paint.color = effect.color
                paint.alpha = ((effect.life ?: 0f) * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(x, y, radius, paint)
                
                // Glow effect
                paint.alpha = ((effect.life ?: 0f) * 60).toInt().coerceIn(0, 60)
                canvas.drawCircle(x, y, radius * 1.5f, paint)
            }
        }
    }
}
