package com.dev.utilix

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.dev.utilix.R

class MainActivity : AppCompatActivity() {

    private lateinit var featureManager: FeatureManager
    
    // UI References
    private lateinit var cardFlashlight: CardView
    private lateinit var cardVibrate: CardView
    private lateinit var cardScreenLight: CardView
    private lateinit var cardWaterEject: CardView
    private lateinit var cardMediaMute: CardView
    private lateinit var cardDice: CardView
    private lateinit var cardTally: CardView
    
    private lateinit var overlayScreenLight: View
    
    private lateinit var textFlashlight: TextView
    private lateinit var textVibrate: TextView
    private lateinit var textMediaMute: TextView
    private lateinit var textDice: TextView
    private lateinit var textTallyCount: TextView

    // States
    private var isVibrating = false
    private var isScreenLightOn = false
    private var tallyCount = 0

    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private val PERMISSION_REQUEST_CODE = 10

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        featureManager = FeatureManager(this)

        initViews()
        setupClickListeners()
        
        findViewById<android.widget.ImageView>(R.id.btnSettings).setOnClickListener {
             startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }
        
        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!allPermissionsGranted()) {
                android.widget.Toast.makeText(this, "Camera permission is needed for Flashlight and Tools.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        cardFlashlight = findViewById(R.id.cardFlashlight)
        cardVibrate = findViewById(R.id.cardVibrate)
        cardScreenLight = findViewById(R.id.cardScreenLight)
        cardWaterEject = findViewById(R.id.cardWaterEject)
        cardMediaMute = findViewById(R.id.cardMediaMute)
        cardDice = findViewById(R.id.cardDice)
        cardTally = findViewById(R.id.cardTally)
        
        overlayScreenLight = findViewById(R.id.overlayScreenLight)
        
        textFlashlight = findViewById(R.id.textFlashlight)
        textVibrate = findViewById(R.id.textVibrate)
        textMediaMute = findViewById(R.id.textMediaMute)
        textDice = findViewById(R.id.textDice)
        textTallyCount = findViewById(R.id.textTallyCount)
    }

    private fun setupClickListeners() {
        // 1. Flashlight
        cardFlashlight.setOnClickListener {
            featureManager.hapticTick()
            val isOn = featureManager.toggleFlashlight()
            updateCardState(cardFlashlight, isOn)
            textFlashlight.text = if (isOn) getString(R.string.status_flash_on) else getString(R.string.action_flashlight)
        }

        // 2. Vibration
        cardVibrate.setOnClickListener {
            featureManager.hapticTick()
            isVibrating = !isVibrating
            featureManager.toggleVibration(isVibrating)
            updateCardState(cardVibrate, isVibrating)
            textVibrate.text = if (isVibrating) getString(R.string.status_stop_vibe) else getString(R.string.action_vibrate)
        }

        // 3. Screen Light
        cardScreenLight.setOnClickListener {
            featureManager.hapticTick()
            toggleScreenLight()
        }
        
        // Tap overlay to exit Screen Light
        overlayScreenLight.setOnClickListener {
            toggleScreenLight()
            featureManager.hapticTick()
        }

        // 4. Water Eject
        cardWaterEject.setOnClickListener {
            featureManager.hapticTick()
            val isPlaying = featureManager.toggleWaterEject()
            updateCardState(cardWaterEject, isPlaying)
        }
        
        // 5. Media Mute
        cardMediaMute.setOnClickListener {
            featureManager.hapticTick()
            val isUnmuted = featureManager.toggleMediaVolume()
            updateCardState(cardMediaMute, isUnmuted)
            textMediaMute.text = if (isUnmuted) getString(R.string.status_sound_on) else getString(R.string.status_muted)
        }
        
        // 6. Dice Roll (Decision)
        cardDice.setOnClickListener {
            featureManager.hapticTick()
            val result = featureManager.rollDice()
            textDice.text = getString(R.string.status_dice_result, result)
            // Reset text after 2 seconds
            cardDice.postDelayed({
                textDice.text = getString(R.string.action_decision)
            }, 2000)
        }
        
        // 6b. Long Press for Coin Flip
        cardDice.setOnLongClickListener {
            featureManager.hapticTick()
            val result = featureManager.flipCoin()
            textDice.text = result
             cardDice.postDelayed({
                textDice.text = getString(R.string.action_decision)
            }, 2000)
            true
        }
        
        // 7. Tally Counter
        cardTally.setOnClickListener {
            featureManager.hapticTick()
            tallyCount++
            textTallyCount.text = tallyCount.toString()
        }
        
        cardTally.setOnLongClickListener {
            featureManager.hapticTick()
            tallyCount = 0
            textTallyCount.text = tallyCount.toString()
            true
        }
        
        // 8. QR Scanner
        findViewById<CardView>(R.id.cardQr).setOnClickListener {
             featureManager.hapticTick()
             val intent = android.content.Intent(this, QrScannerActivity::class.java)
             startActivity(intent)
        }
        
        // 9. Mirror
        findViewById<CardView>(R.id.cardMirror).setOnClickListener {
             featureManager.hapticTick()
             val intent = android.content.Intent(this, MirrorActivity::class.java)
             startActivity(intent)
        }

        // 10. Magnifier
        findViewById<CardView>(R.id.cardMagnifier).setOnClickListener {
             featureManager.hapticTick()
             val intent = android.content.Intent(this, MagnifierActivity::class.java)
             startActivity(intent)
        }
        
        // 11. Ruler & Level
        findViewById<CardView>(R.id.cardRuler).setOnClickListener {
             featureManager.hapticTick()
             val intent = android.content.Intent(this, RulerLevelActivity::class.java)
             startActivity(intent)
        }
        
        // 12. Dead Pixel
        findViewById<CardView>(R.id.cardDeadPixel).setOnClickListener {
             featureManager.hapticTick()
             val intent = android.content.Intent(this, DeadPixelActivity::class.java)
             startActivity(intent)
        }
        
        // 13. Keep Awake
        val cardKeepAwake = findViewById<CardView>(R.id.cardKeepAwake)
        val textKeepAwake = findViewById<android.widget.TextView>(R.id.textKeepAwake)
        
        cardKeepAwake.setOnClickListener {
            val isAwake = featureManager.toggleKeepAwake(window)
            if (isAwake) {
                updateCardState(cardKeepAwake, true)
                textKeepAwake.text = getString(R.string.status_awake_on)
            } else {
                updateCardState(cardKeepAwake, false)
                textKeepAwake.text = getString(R.string.action_screen_on)
            }
            featureManager.hapticTick()
        }
    }

    private fun updateCardState(card: CardView, isActive: Boolean) {
        // Find the ImageView inside the card (assuming it's the first child of the LinearLayout)
        // Or better, we should have references. Instead of passing ImageView, let's find it.
        // Or update the signature to accept ImageView.
        // BUT, existing calls only pass card.
        // Let's rely on finding it by ID if possible, but we don't know which card it is easily.
        // Let's traverse: Card -> LinearLayout -> ImageView
        
        val linearLayout = card.getChildAt(0) as? android.widget.LinearLayout
        val imageView = linearLayout?.getChildAt(0) as? android.widget.ImageView
        
        if (isActive) {
            // Yellow Card
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.accent_yellow))
            // White Circle Container
            imageView?.setBackgroundResource(R.drawable.bg_circle_active)
        } else {
            // White Card
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
            // Grey Stroke Circle Container
            imageView?.setBackgroundResource(R.drawable.bg_circle_inactive)
        }
    }

    private fun toggleScreenLight() {
        isScreenLightOn = !isScreenLightOn
        
        if (isScreenLightOn) {
            overlayScreenLight.visibility = View.VISIBLE
            // Max brightness
            val layoutParams = window.attributes
            layoutParams.screenBrightness = 1.0f
            window.attributes = layoutParams
        } else {
            overlayScreenLight.visibility = View.GONE
            // Restore brightness
            val layoutParams = window.attributes
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = layoutParams
        }
    }
}
