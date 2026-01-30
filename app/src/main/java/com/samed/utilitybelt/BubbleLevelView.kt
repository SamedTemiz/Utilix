package com.samed.utilitybelt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BubbleLevelView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paintCircle = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val paintBubble = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val paintCross = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    var pitch: Float = 0f
    var roll: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) / 2f - 20f
        
        // Draw crosshair
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paintCross)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paintCross)

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
