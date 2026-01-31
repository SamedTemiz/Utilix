package com.dev.utilix

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.utilix.databinding.ActivityRulerLevelBinding
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

class RulerLevelActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityRulerLevelBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private lateinit var cameraExecutor: java.util.concurrent.ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulerLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        cameraExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()
        startCamera()
        
        binding.btnClose.setOnClickListener { finish() }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: androidx.camera.lifecycle.ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                // Handle errors
            }

        }, androidx.core.content.ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate Pitch and Roll from Accelerometer directly
            // Pitch: Rotation around X-axis
            // Roll: Rotation around Y-axis
            // Note: Standard mapping might need adjustment for "flat on table" vs "portrait hold"
            // For a bubble level flat on table:
            // X axis is left/right. Y axis is top/bottom. Z is up/down.
            
            // atan2 returns radians. Convert to degrees.
            // pitch = atan2(y, z) or similar?
            
            // Standard formulas:
            // pitch = atan2(-x, sqrt(y*y + z*z)) * 180 / PI
            // roll = atan2(y, z) * 180 / PI
            
            // Let's use the gravity vector directly. 
            // If phone is flat: x=0, y=0, z=9.8. Pitch=0, Roll=0.
            // If tilted right (x positive?): 
            
            val pitch = (Math.atan2((-x).toDouble(), Math.sqrt((y * y + z * z).toDouble())) * 180 / Math.PI).toFloat()
            val roll = (Math.atan2(y.toDouble(), z.toDouble()) * 180 / Math.PI).toFloat()

            binding.bubbleLevelView.pitch = roll // Swap/adjust based on view testing. 
            // Actually, let's map correctly:
            // If I tilt top up (y increases?), I want bubble to go up.
            // If I tilt right up (x decreases?), I want bubble left.
            
            // Let's pass raw values and let the view handle or pass computed degrees.
            // My View expects 'pitch' and 'roll' in degrees.
            
            // Let's try:
            binding.bubbleLevelView.pitch = -roll // Y-axis tilt
            binding.bubbleLevelView.roll = -pitch // X-axis tilt (roughly)

            binding.bubbleLevelView.invalidate()

            val maxAngle = maxOf(abs(pitch), abs(roll))
            binding.textLevelInfo.text = "${maxAngle.roundToInt()}°"

            if (abs(pitch) < 3 && abs(roll) < 3) { // Increased tolerance slightly
                binding.textLevelInfo.setTextColor(android.graphics.Color.parseColor("#FF8C66"))
            } else {
                binding.textLevelInfo.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    private fun updateOrientationAngles() {
        // Deprecated/Unused in this new accelerometer-only logic
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}
