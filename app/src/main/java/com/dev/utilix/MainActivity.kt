package com.dev.utilix

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dev.utilix.FeatureItem
import com.dev.utilix.R
import java.util.Collections



class MainActivity : AppCompatActivity() {

    private lateinit var featureManager: FeatureManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeatureAdapter
    private lateinit var featureList: MutableList<FeatureItem>
    private lateinit var sharedPreferences: SharedPreferences
    
    // States
    private var isVibrating = false
    private var isScreenLightOn = false
    private var tallyCount = 0
    private lateinit var overlayScreenLight: View

    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private val PERMISSION_REQUEST_CODE = 10
    
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check Onboarding
        val pm = PreferenceManager(this)
        if (pm.isFirstRun) {
            startActivity(android.content.Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        featureManager = FeatureManager(this)
        sharedPreferences = getSharedPreferences("FeaturePrefs", Context.MODE_PRIVATE)
        overlayScreenLight = findViewById(R.id.overlayScreenLight)

        setupRecyclerView()
        
        findViewById<android.widget.ImageView>(R.id.btnSettings).setOnClickListener {
             startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }
        
        // Tap overlay to exit Screen Light
        overlayScreenLight.setOnClickListener {
            toggleScreenLight()
            featureManager.hapticTick()
        }
        
        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewFeatures)
        featureList = getInitialFeatureList()
        
        // Restore Order from Prefs
        val savedOrder = sharedPreferences.getString("FeatureOrder", null)
        if (savedOrder != null) {
            val orderList = savedOrder.split(",").filter { it.isNotEmpty() }
            val sortedList = mutableListOf<FeatureItem>()
            val itemMap = featureList.associateBy { it.id }
            
            // Add items in saved order
            orderList.forEach { id ->
                itemMap[id]?.let { sortedList.add(it) }
            }
            
            // Add any new items not in saved order
            featureList.forEach { item ->
                if (!sortedList.contains(item)) {
                    sortedList.add(item)
                }
            }
            featureList = sortedList
        }

        adapter = FeatureAdapter(
            this, 
            featureList,
            onFeatureClick = { item, position -> handleFeatureClick(item, position) },
            onFeatureLongClick = { item, position -> handleFeatureLongClick(item, position) },
            onItemMove = { from, to -> 
                 saveOrder()
            }
        )
        
        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // If it's the last item and total items is odd, span 2 columns
                return if (position == adapter.itemCount - 1 && adapter.itemCount % 2 != 0) 2 else 1
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                adapter.moveItem(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe action
            }
            
            override fun isLongPressDragEnabled(): Boolean {
                 return true // Standard Long Press to Drag
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun getInitialFeatureList(): MutableList<FeatureItem> {
        return mutableListOf(
            FeatureItem("flashlight", R.string.action_flashlight, R.drawable.ic_flashlight),
            FeatureItem("vibrate", R.string.action_vibrate, R.drawable.ic_vibration),
            FeatureItem("screen_light", R.string.action_screen_light, R.drawable.ic_screen_light),
            FeatureItem("water_eject", R.string.action_water_eject, R.drawable.ic_water_drop),
            FeatureItem("media_mute", R.string.action_system, R.drawable.ic_mute), 
            FeatureItem("dice", R.string.action_decision, R.drawable.ic_dice),
            FeatureItem("tally", R.string.action_counter, R.drawable.ic_counter),
            FeatureItem("qr", R.string.action_qr, R.drawable.ic_qr_scan),
            FeatureItem("mirror", R.string.action_mirror, R.drawable.ic_mirror),
            FeatureItem("magnifier", R.string.action_magnifier, R.drawable.ic_magnifier),
            FeatureItem("ruler", R.string.action_ruler, R.drawable.ic_ruler),
            FeatureItem("dead_pixel", R.string.action_pixel_test, R.drawable.ic_pixel_test),
            FeatureItem("keep_awake", R.string.action_screen_on, R.drawable.ic_keep_awake)
        )
    }
    
    private fun saveOrder() {
        val orderString = featureList.joinToString(",") { it.id }
        sharedPreferences.edit().putString("FeatureOrder", orderString).apply()
    }

    private fun handleFeatureClick(item: FeatureItem, position: Int) {
         when (item.id) {
            "flashlight" -> {
                featureManager.hapticTick()
                val isOn = featureManager.toggleFlashlight()
                item.isActive = isOn
                item.activeText = if (isOn) getString(R.string.status_flash_on) else null
                adapter.notifyItemChanged(position)
            }
            "vibrate" -> {
                featureManager.hapticTick()
                isVibrating = !isVibrating
                featureManager.toggleVibration(isVibrating)
                item.isActive = isVibrating
                item.activeText = if (isVibrating) getString(R.string.status_stop_vibe) else null
                adapter.notifyItemChanged(position)
            }
            "screen_light" -> {
                featureManager.hapticTick()
                toggleScreenLight()
            }
            "water_eject" -> {
                featureManager.hapticTick()
                val isPlaying = featureManager.toggleWaterEject()
                item.isActive = isPlaying
                adapter.notifyItemChanged(position)
            }
            "media_mute" -> {
                featureManager.hapticTick()
                val isUnmuted = featureManager.toggleMediaVolume()
                item.isActive = isUnmuted
                item.activeText = if (isUnmuted) getString(R.string.status_sound_on) else getString(R.string.status_muted)
                adapter.notifyItemChanged(position)
            }
            "dice" -> {
                featureManager.hapticTick()
                val result = featureManager.rollDice()
                // Show result briefly
                item.activeText = getString(R.string.status_dice_result, result)
                adapter.notifyItemChanged(position)
                
                recyclerView.postDelayed({
                    item.activeText = null
                    adapter.notifyItemChanged(position)
                }, 2000)
            }
            "tally" -> {
                featureManager.hapticTick()
                tallyCount++
                item.activeText = tallyCount.toString()
                adapter.notifyItemChanged(position)
            }
            "qr" -> {
                featureManager.hapticTick()
                startActivity(android.content.Intent(this, QrScannerActivity::class.java))
            }
            "mirror" -> {
                featureManager.hapticTick()
                startActivity(android.content.Intent(this, MirrorActivity::class.java))
            }
            "magnifier" -> {
                featureManager.hapticTick()
                startActivity(android.content.Intent(this, MagnifierActivity::class.java))
            }
            "ruler" -> {
                featureManager.hapticTick()
                startActivity(android.content.Intent(this, RulerLevelActivity::class.java))
            }
            "dead_pixel" -> {
                featureManager.hapticTick()
                startActivity(android.content.Intent(this, DeadPixelActivity::class.java))
            }
            "keep_awake" -> {
                val isAwake = featureManager.toggleKeepAwake(window)
                item.isActive = isAwake
                item.activeText = if (isAwake) getString(R.string.status_awake_on) else null
                adapter.notifyItemChanged(position)
                featureManager.hapticTick()
            }
         }
    }
    
    private fun handleFeatureLongClick(item: FeatureItem, position: Int): Boolean {
        // Special long press actions (Coin Flip, Reset Tally)
        // DISABLED to allow Drag & Drop for all items.
        /*
        when (item.id) {
            "dice" -> {
                featureManager.hapticTick()
                val result = featureManager.flipCoin()
                item.activeText = result
                adapter.notifyItemChanged(position)
                recyclerView.postDelayed({
                    item.activeText = null
                    adapter.notifyItemChanged(position)
                }, 2000)
                return true
            }
            "tally" -> {
                featureManager.hapticTick()
                tallyCount = 0
                item.activeText = tallyCount.toString()
                adapter.notifyItemChanged(position)
                return true
            }
        }
        */
        return false // Let ItemTouchHelper handle it (Drag & Drop)
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

