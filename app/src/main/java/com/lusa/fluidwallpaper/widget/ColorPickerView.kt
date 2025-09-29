package com.lusa.fluidwallpaper.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val satValPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var hueRect = RectF()
    private var satValRect = RectF()
    private var borderRect = RectF()
    
    private var currentHue = 0f
    private var currentSaturation = 1f
    private var currentValue = 1f
    
    private var onColorSelectedListener: ((Int) -> Unit)? = null
    
    init {
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
        borderPaint.color = Color.WHITE
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val margin = 20f
        val hueWidth = 60f
        val satValSize = minOf(w - hueWidth - 3 * margin, h - 2 * margin)
        
        hueRect = RectF(
            margin,
            margin,
            margin + hueWidth,
            margin + satValSize
        )
        
        satValRect = RectF(
            hueRect.right + margin,
            margin,
            hueRect.right + margin + satValSize,
            margin + satValSize
        )
        
        borderRect = RectF(
            margin - 2f,
            margin - 2f,
            w - margin + 2f,
            h - margin + 2f
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw hue bar
        drawHueBar(canvas)
        
        // Draw saturation/value square
        drawSaturationValueSquare(canvas)
        
        // Draw border
        canvas.drawRoundRect(borderRect, 8f, 8f, borderPaint)
    }
    
    private fun drawHueBar(canvas: Canvas) {
        val colors = intArrayOf(
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
            Color.BLUE, Color.MAGENTA, Color.RED
        )
        
        val shader = LinearGradient(
            0f, hueRect.top, 0f, hueRect.bottom,
            colors, null, Shader.TileMode.CLAMP
        )
        
        huePaint.shader = shader
        canvas.drawRoundRect(hueRect, 8f, 8f, huePaint)
        
        // Draw hue indicator
        val hueY = hueRect.top + currentHue * (hueRect.height() / 360f)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(hueRect.right + 10f, hueY, 8f, paint)
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.BLACK
        canvas.drawCircle(hueRect.right + 10f, hueY, 8f, paint)
    }
    
    private fun drawSaturationValueSquare(canvas: Canvas) {
        // Create HSV to RGB conversion shader
        val colors = intArrayOf(
            Color.WHITE,
            Color.HSVToColor(floatArrayOf(currentHue, 1f, 1f)),
            Color.BLACK,
            Color.HSVToColor(floatArrayOf(currentHue, 1f, 0f))
        )
        
        val positions = floatArrayOf(0f, 1f, 1f, 0f)
        
        val shader = LinearGradient(
            satValRect.left, satValRect.top, satValRect.right, satValRect.top,
            colors, positions, Shader.TileMode.CLAMP
        )
        
        satValPaint.shader = shader
        canvas.drawRoundRect(satValRect, 8f, 8f, satValPaint)
        
        // Draw saturation/value indicator
        val satX = satValRect.left + currentSaturation * satValRect.width()
        val valY = satValRect.top + (1f - currentValue) * satValRect.height()
        
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(satX, valY, 12f, paint)
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.BLACK
        canvas.drawCircle(satX, valY, 12f, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                
                if (hueRect.contains(x, y)) {
                    currentHue = ((y - hueRect.top) / hueRect.height() * 360f).coerceIn(0f, 360f)
                    invalidate()
                    notifyColorChanged()
                    return true
                } else if (satValRect.contains(x, y)) {
                    currentSaturation = ((x - satValRect.left) / satValRect.width()).coerceIn(0f, 1f)
                    currentValue = 1f - ((y - satValRect.top) / satValRect.height()).coerceIn(0f, 1f)
                    invalidate()
                    notifyColorChanged()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun notifyColorChanged() {
        val color = Color.HSVToColor(floatArrayOf(currentHue, currentSaturation, currentValue))
        onColorSelectedListener?.invoke(color)
    }
    
    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        currentHue = hsv[0]
        currentSaturation = hsv[1]
        currentValue = hsv[2]
        invalidate()
        notifyColorChanged()
    }
    
    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelectedListener = listener
    }
    
    fun getCurrentColor(): Int {
        return Color.HSVToColor(floatArrayOf(currentHue, currentSaturation, currentValue))
    }
}
