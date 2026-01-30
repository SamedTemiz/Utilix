package com.samed.utilitybelt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.samed.utilitybelt.databinding.ActivityRulerLevelBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulerLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        binding.btnClose.setOnClickListener { finish() }
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
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        updateOrientationAngles()
    }

    private fun updateOrientationAngles() {
        // Update rotation matrix
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // Get orientation
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert radians to degrees
        // orientationAngles[1] = pitch (x-axis incline)
        // orientationAngles[2] = roll (y-axis incline)
        
        val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        binding.bubbleLevelView.pitch = pitch
        binding.bubbleLevelView.roll = roll
        binding.bubbleLevelView.invalidate()
        
        val maxAngle = maxOf(abs(pitch), abs(roll))
        binding.textLevelInfo.text = "${maxAngle.roundToInt()}°"
        
        if (abs(pitch) < 2 && abs(roll) < 2) {
            binding.textLevelInfo.setTextColor(android.graphics.Color.GREEN)
        } else {
            binding.textLevelInfo.setTextColor(android.graphics.Color.WHITE)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}
