package com.dev.utilix

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dev.utilix.databinding.ActivityDeadPixelBinding

class DeadPixelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeadPixelBinding
    private val colors = arrayOf(
        Color.WHITE,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.BLACK
    )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen mode
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // Hide ActionBar
        supportActionBar?.hide()

        binding = ActivityDeadPixelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start with White
        binding.container.setBackgroundColor(colors[0])
        
        binding.container.setOnClickListener {
            binding.textInstruction.visibility = View.GONE
            colorIndex = (colorIndex + 1)
            
            if (colorIndex >= colors.size) {
                // Loop or exit? Let's cycle.
                colorIndex = 0
            }
            binding.container.setBackgroundColor(colors[colorIndex])
        }
        
        // Long click to finish
        binding.container.setOnLongClickListener {
            finish()
            true
        }
    }
}
