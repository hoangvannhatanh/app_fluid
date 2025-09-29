package com.lusa.fluidwallpaper.renderer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*
import kotlin.random.Random

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
    
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var color: Int,
        var life: Float
    )
    
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
                touchCallback?.invoke(x, y)
                return true
            }
        }
        return super.onTouchEvent(event)
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
                
                // Only render Particle Flow effect
                updateAndDrawParticles(canvas)
                
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
        return Particle(
            x = Random.nextFloat(),
            y = Random.nextFloat(),
            vx = (Random.nextFloat() - 0.5f) * 0.02f,
            vy = (Random.nextFloat() - 0.5f) * 0.02f,
            size = Random.nextFloat() * 20f + 10f,
            color = getRandomColor(),
            life = 1f
        )
    }
    
    private fun getRandomColor(): Int {
        val colors = intArrayOf(
            Color.parseColor("#FF2196F3"), // Blue
            Color.parseColor("#FF9C27B0"), // Purple
            Color.parseColor("#FF4CAF50"), // Green
            Color.parseColor("#FFF44336"), // Red
            Color.parseColor("#FFFF9800"), // Orange
            Color.parseColor("#FF00BCD4")  // Cyan
        )
        return colors[Random.nextInt(colors.size)]
    }
    
    private fun updateAndDrawParticles(canvas: Canvas) {
        updateParticles()
        drawParticles(canvas)
    }
    
    
    private fun updateParticles() {
        particles.forEach { particle ->
            // Update position
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
            
            // Update life
            particle.life -= 0.01f
            if (particle.life <= 0f) {
                particles.remove(particle)
                particles.add(createRandomParticle())
            }
        }
    }
    
    private fun drawParticles(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        particles.forEach { particle ->
            paint.color = particle.color
            paint.alpha = (particle.life * 255).toInt()
            
            val x = particle.x * width
            val y = particle.y * height
            val radius = particle.size * particle.life
            
            canvas.drawCircle(x, y, radius, paint)
            
            // Add glow effect
            paint.alpha = (particle.life * 100).toInt()
            canvas.drawCircle(x, y, radius * 2, paint)
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
