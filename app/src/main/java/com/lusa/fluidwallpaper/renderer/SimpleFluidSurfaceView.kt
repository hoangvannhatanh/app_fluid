package com.lusa.fluidwallpaper.renderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*
import kotlin.random.Random
import com.lusa.fluidwallpaper.model.Particle
import com.lusa.fluidwallpaper.utils.ColorPreferences

class SimpleFluidSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    
    private var renderThread: Thread? = null
    private var shouldRender = false
    private var surfaceHolder: SurfaceHolder = holder
    
    private var touchCallback: ((Float, Float) -> Unit)? = null
    private var gyroscopeCallback: ((Float, Float, Float) -> Unit)? = null
    
    // Effect parameters
    private var effectType = 0
    private var speed = 1.0f
    private var viscosity = 1.0f
    private var turbulence = 0.5f
    private var color1 = floatArrayOf(0.2f, 0.6f, 1.0f)
    private var color2 = floatArrayOf(0.8f, 0.2f, 0.9f)
    private var batterySaveMode = false
    
    // Animation variables
    private var time = 0f
    private val particles = mutableListOf<Particle>()
    private val maxParticles = 30
    
    // Touch interaction variables
    private val touchEffects = mutableListOf<Particle>()
    private var isTouching = false
    private var touchX = 0f
    private var touchY = 0f
    private var lastColorUpdateTime = 0L
    private var lastKnownUpdateTime = 0L
    
    init {
        holder.addCallback(this)
        initializeParticles()
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        startRendering()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface changed - restart rendering if needed
        if (!shouldRender) {
            startRendering()
        }
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRendering()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x / width
                val y = event.y / height
                
                // Clamp coordinates to valid range
                val clampedX = x.coerceIn(0f, 1f)
                val clampedY = y.coerceIn(0f, 1f)
                
                isTouching = true
                touchX = clampedX
                touchY = clampedY
                
                // Create touch effects
                if (event.action == MotionEvent.ACTION_DOWN) {
                    createTouchBurst(clampedX, clampedY)
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    createTouchTrail(clampedX, clampedY)
                }
                
                touchCallback?.invoke(clampedX, clampedY)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                createTouchRelease(touchX, touchY)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun createTouchBurst(x: Float, y: Float) {
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
    
    private fun createTouchTrail(x: Float, y: Float) {
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
    
    private fun createTouchRelease(x: Float, y: Float) {
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
    
    private fun startRendering() {
        if (shouldRender) return // Already rendering
        
        shouldRender = true
        renderThread = Thread {
            while (shouldRender && !Thread.currentThread().isInterrupted) {
                try {
                    renderFrame()
                    
                    // Control frame rate
                    val fps = if (batterySaveMode) 30 else 60
                    Thread.sleep((1000 / fps).toLong())
                } catch (e: Exception) {
                    Log.e("SimpleFluidSurfaceView", "Rendering error", e)
                    break
                }
            }
        }
        renderThread?.start()
    }
    
    private fun stopRendering() {
        shouldRender = false
        renderThread?.interrupt()
        try {
            renderThread?.join(1000) // Wait max 1 second
        } catch (e: InterruptedException) {
            // Thread interrupted, that's fine
        }
        renderThread = null
    }
    
    private fun renderFrame() {
        val canvas = surfaceHolder.lockCanvas()
        if (canvas != null) {
            try {
                // Clear canvas with dark background
                canvas.drawColor(Color.BLACK)
                
                // Check for color updates every 500ms
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastColorUpdateTime > 500) {
                    checkForColorUpdates()
                    lastColorUpdateTime = currentTime
                }
                
                // Update and draw particles
                updateParticles()
                drawParticles(canvas)
                
                // Update and draw touch effects
                updateTouchEffects()
                drawTouchEffects(canvas)
                
                // Update time
                time += 0.016f
                
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }
    
    private fun initializeParticles() {
        particles.clear()
        repeat(maxParticles) {
            particles.add(createRandomParticle())
        }
    }
    
    private fun createRandomParticle(): Particle {
        val angle = Random.nextFloat() * 2 * kotlin.math.PI
        val speed = 0.04f
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
    
    private fun getRandomColor(): Int {
        // Use the colors from ViewModel instead of fixed colors
        val colorArray = if (Random.nextBoolean()) color1 else color2
        return Color.rgb(
            (colorArray[0] * 255).toInt(),
            (colorArray[1] * 255).toInt(),
            (colorArray[2] * 255).toInt()
        )
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
            particle.x += particle.vx * speed
            particle.y += particle.vy * speed
            
            // Add turbulence
            particle.vx += (Random.nextFloat() - 0.5f) * turbulence * 0.01f
            particle.vy += (Random.nextFloat() - 0.5f) * turbulence * 0.01f
            
            // Apply viscosity (friction)
            particle.vx *= (1f - viscosity * 0.01f)
            particle.vy *= (1f - viscosity * 0.01f)
            
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
    
    private fun drawParticles(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        // Create a copy to avoid ConcurrentModificationException
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
    
    private fun checkForColorUpdates() {
        // Check if colors have been updated by checking timestamp
        val currentUpdateTime = ColorPreferences.getLastUpdateTime(context)
        
        if (currentUpdateTime > lastKnownUpdateTime) {
            Log.d("SimpleFluidSurfaceView", "Colors updated, reloading...")
            loadColors()
            refreshParticles()
        }
    }
    
    private fun loadColors() {
        color1 = ColorPreferences.getColor1(context)
        color2 = ColorPreferences.getColor2(context)
        lastKnownUpdateTime = ColorPreferences.getLastUpdateTime(context)
    }
    
    private fun refreshParticles() {
        // Update existing particles with new colors
        particles.forEach { particle ->
            particle.color = getRandomColor()
        }
    }
    
    // Public methods to control the effect
    fun setTouchCallback(callback: (Float, Float) -> Unit) {
        touchCallback = callback
    }
    
    fun setGyroscopeCallback(callback: (Float, Float, Float) -> Unit) {
        gyroscopeCallback = callback
    }
    
    fun setEffectType(type: Int) {
        effectType = type
    }
    
    fun setSpeed(speed: Float) {
        this.speed = speed
    }
    
    fun setViscosity(viscosity: Float) {
        this.viscosity = viscosity
    }
    
    fun setTurbulence(turbulence: Float) {
        this.turbulence = turbulence
    }
    
    fun setColors(color1: FloatArray, color2: FloatArray) {
        this.color1 = color1.clone()
        this.color2 = color2.clone()
        refreshParticles()
    }
    
    fun setBatterySaveMode(enabled: Boolean) {
        batterySaveMode = enabled
    }
    
    fun setTouchPosition(x: Float, y: Float) {
        // Add explosion effect at touch point
        repeat(5) {
            particles.add(Particle(
                x = x,
                y = y,
                vx = (Random.nextFloat() - 0.5f) * 0.1f,
                vy = (Random.nextFloat() - 0.5f) * 0.1f,
                size = Random.nextFloat() * 30f + 20f,
                color = getRandomColor(),
                life = 1f
            ))
        }
    }
    
    fun setGyroscopeData(x: Float, y: Float, z: Float) {
        // Apply gyroscope data to particle movement
        particles.forEach { particle ->
            particle.vx += x * 0.001f
            particle.vy += y * 0.001f
        }
    }
    
    fun pause() {
        stopRendering()
    }

    fun resume() {
        if (surfaceHolder.surface.isValid) {
            startRendering()
        }
    }
}