package com.samed.utilitybelt

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        featureManager = FeatureManager(this)

        initViews()
        setupClickListeners()
        
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
            textFlashlight.text = if (isOn) "FLASH ON" else getString(R.string.action_flashlight)
        }

        // 2. Vibration
        cardVibrate.setOnClickListener {
            featureManager.hapticTick()
            isVibrating = !isVibrating
            featureManager.toggleVibration(isVibrating)
            updateCardState(cardVibrate, isVibrating)
            textVibrate.text = if (isVibrating) "STOP VIBE" else getString(R.string.action_vibrate)
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
            // If isUnmuted is true, it means we are HEALING audio (Active Green). 
            // If isUnmuted is false, we are MUTED (Grey? Or Red? Let's use Grey for muted)
            // Logic flip: If Muted, we probably want to show it's active "MUTER". 
            // Let's say: Green = Sound ON, Grey = Sound OFF (Muted).
            
            updateCardState(cardMediaMute, isUnmuted)
            textMediaMute.text = if (isUnmuted) "SOUND ON" else "MUTED"
        }
        
        // 6. Dice Roll (Decision)
        cardDice.setOnClickListener {
            featureManager.hapticTick()
            val result = featureManager.rollDice()
            textDice.text = "DICE: $result"
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
    }

    private fun updateCardState(card: CardView, isActive: Boolean) {
        if (isActive) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.active_green))
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
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
