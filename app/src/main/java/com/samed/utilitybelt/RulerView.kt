package com.samed.utilitybelt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View

class RulerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        textSize = 30f
        isAntiAlias = true
    }
    
    private val pixelsPerMm: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_MM,
        1f,
        context.resources.displayMetrics
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height.toFloat()
        val width = width.toFloat()

        // Draw ruler lines every mm
        var currentX = 0f
        var mmCount = 0

        while (currentX < width) {
            val lineHeight = if (mmCount % 10 == 0) {
                height * 0.5f // Every cm
            } else if (mmCount % 5 == 0) {
                height * 0.3f // Every 5mm
            } else {
                height * 0.15f // Every 1mm
            }

            canvas.drawLine(currentX, height, currentX, height - lineHeight, paint)

            if (mmCount % 10 == 0) {
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("${mmCount / 10}", currentX + 4f, height - lineHeight + 30f, paint)
            }

            currentX += pixelsPerMm
            mmCount++
        }
    }
}
