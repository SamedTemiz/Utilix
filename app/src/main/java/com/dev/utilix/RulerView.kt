package com.dev.utilix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.roundToInt

class RulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Configuration
    private var unitDistance = 0f // Pixels per unit (e.g., mm)
    private var minValue = 0
    private var maxValue = 100 // cm
    private var currentValue = 50f // cm (start middle)

    // Drawing
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Interaction
    private val scroller = Scroller(context)
    private val velocityTracker = VelocityTracker.obtain()
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private var lastX = 0f
    private var isDragging = false
    private var viewWidth = 0
    private var viewHeight = 0

    // Dimensions
    private var infoTextSize = 40f
    private var longLineHeight = 60f
    private var shortLineHeight = 30f
    
    // Total scroll range in pixels
    private var maxScrollDistance = 0

    init {
        // Calculate pixels per mm based on screen density for accuracy
        val dm = resources.displayMetrics
        // physical pixels per inch / 25.4 (mm per inch) = pixels per mm
        // xdpi is exact physical pixels per inch in X dimension
        val xdpi = dm.xdpi
        unitDistance = xdpi / 25.4f * 10f // 10mm = 1cm gap for main lines? 
        // Let's say unitDistance is for 1mm markings. 
        // 1mm = xdpi/25.4
        unitDistance = xdpi / 25.4f

        linePaint.color = Color.WHITE
        linePaint.strokeWidth = 4f
        linePaint.style = Paint.Style.STROKE

        textPaint.color = Color.WHITE
        textPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, dm)
        textPaint.textAlign = Paint.Align.CENTER
        
        indicatorPaint.color = Color.RED
        indicatorPaint.strokeWidth = 6f
        indicatorPaint.style = Paint.Style.STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        
        longLineHeight = h * 0.6f
        shortLineHeight = h * 0.3f
        
        // Calculate range
        // Total width needed = total millimeters * unitDistance
        // We want accurate ruler.
        // Range 0 to 100 cm = 1000 mm.
        val totalMm = (maxValue - minValue) * 10 
        maxScrollDistance = (totalMm * unitDistance).toInt()
        
        // Initial scroll to current value
        val initialScrollX = ((currentValue - minValue) * 10 * unitDistance).toInt()
        scrollTo(initialScrollX, 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scrollX = scrollX
        val centerX = scrollX + viewWidth / 2f
        
        // Draw Indicator
        canvas.drawLine(centerX, 0f, centerX, viewHeight.toFloat(), indicatorPaint)

        // Draw Scale
        // We draw lines for every mm.
        // Start index based on visible area optimization
        val startMm = ((scrollX - viewWidth / 2) / unitDistance).toInt() - 10
        val endMm = ((scrollX + viewWidth * 1.5) / unitDistance).toInt() + 10

        for (i in startMm..endMm) {
            val positionX = i * unitDistance
            
            // Logic for lines
            // Metric: i is mm. Every 10 is cm.
            // Imperial: i is 0.1 inch. Every 10 is 1 inch.
            
            val isMajor = i % 10 == 0
            val isMid = i % 5 == 0 && !isMajor

            val lineHeight = if (isMajor) longLineHeight else if (isMid) shortLineHeight * 1.5f else shortLineHeight
            
            val startY = viewHeight - lineHeight
            
            // Draw axis at 'i' units from 0.
            // Note: In onSizeChanged we scrollTo appropriate offset.
            // Here we just draw line at positionX (relative to scroll). 
            // BUT: scrollX is already offset by viewWidth/2? No.
            // Let's stick to: drawX = positionX + viewWidth / 2f
            
            val drawX = positionX + viewWidth / 2f
            
            canvas.drawLine(drawX, startY, drawX, viewHeight.toFloat(), linePaint)

            if (isMajor) {
                // If metric, i/10 = cm value
                // If imperial, i/10 = inch value
                val textValue = i / 10
                canvas.drawText("$textValue", drawX, startY - 20, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event)
        val x = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
                lastX = x
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                if (!isDragging && abs(dx) > touchSlop) {
                    isDragging = true
                }
                if (isDragging) {
                    scrollBy((-dx).toInt(), 0)
                    lastX = x
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    val velocityX = velocityTracker.xVelocity.toInt()
                    if (abs(velocityX) > minFlingVelocity) {
                        fling(-velocityX)
                    } else {
                        snapToNearest()
                    }
                } else {
                    snapToNearest()
                }
                isDragging = false
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!scroller.isFinished) scroller.abortAnimation()
                snapToNearest()
                isDragging = false
            }
        }
        return true
    }

    private fun fling(velocityX: Int) {
        scroller.fling(
            scrollX, 0,
            velocityX, 0,
            0, maxScrollDistance,
            0, 0
        )
        postInvalidateOnAnimation()
    }

    private fun snapToNearest() {
        // Optional: snap to nearest mm or cm
        // For now, simple deceleration is fine, but let's ensure bounds
        if (scrollX < 0) {
            scroller.startScroll(scrollX, 0, -scrollX, 0)
            postInvalidateOnAnimation()
        } else if (scrollX > maxScrollDistance) {
            scroller.startScroll(scrollX, 0, maxScrollDistance - scrollX, 0)
            postInvalidateOnAnimation()
        }
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            postInvalidateOnAnimation()
            
            // Update values while scrolling (optional listener call)
        }
    }
    
    override fun scrollTo(x: Int, y: Int) {
        // Clamp
        val clampedX = x.coerceIn(0, maxScrollDistance)
        super.scrollTo(clampedX, y)
    }
}
