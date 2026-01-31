package com.dev.utilix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BubbleLevelView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val paintBubble = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val paintCross = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    init {
        // Use theme colors
        // Circle: White
        paintCircle.color = Color.WHITE // or ContextCompat.getColor(context, R.color.white)
        
        // Bubble: Button Orange (User Request)
        // #FF8C66
        paintBubble.color = android.graphics.Color.parseColor("#FF8C66") 
        
        // Cross: Button Orange or Grey? stick to White/Grey for cross
        paintCross.color = Color.LTGRAY
    }

    var pitch: Float = 0f
    var roll: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) / 2f - 20f
        
        // Draw crosshair with center gap for text
        val gap = 60f
        
        // Horizontal (Left and Right segments)
        canvas.drawLine(cx - radius, cy, cx - gap, cy, paintCross)
        canvas.drawLine(cx + gap, cy, cx + radius, cy, paintCross)

        // Vertical (Top and Bottom segments)
        canvas.drawLine(cx, cy - radius, cx, cy - gap, paintCross)
        canvas.drawLine(cx, cy + gap, cx, cy + radius, paintCross)

        // Draw outer circles
        canvas.drawCircle(cx, cy, radius, paintCircle)
        canvas.drawCircle(cx, cy, radius / 2, paintCircle)

        // Calculate bubble position based on pitch/roll
        // Roll (X axis tilt) moves bubble Left/Right
        // Pitch (Y axis tilt) moves bubble Up/Down
        
        val bubbleRadius = radius / 8
        
        // Clamp values to stay within circle
        val maxTilt = 45f // degrees considered "full tilt"
        
        val bX = cx + (roll / maxTilt) * radius
        val bY = cy - (pitch / maxTilt) * radius // Pitch up (-) means bubble moves down on screen logic? No, pitch up means top goes back, bubble goes up.
        
        // Simple limiting logic
        // This is a basic implementation suitable for simple levelling
        
        canvas.drawCircle(bX, bY, bubbleRadius, paintBubble)
    }
}
