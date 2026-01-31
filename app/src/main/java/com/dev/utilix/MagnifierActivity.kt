package com.dev.utilix

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dev.utilix.databinding.ActivityMagnifierBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MagnifierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMagnifierBinding
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var isFlashOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMagnifierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnClose.setOnClickListener { finish() }

        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }

        binding.seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (camera != null) {
                    val maxZoom = camera!!.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                    val minZoom = camera!!.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
                    
                    // Linear interpolation
                    val zoomRatio = minZoom + (progress / 100f) * (maxZoom - minZoom)
                    camera!!.cameraControl.setZoomRatio(zoomRatio)
                    
                    binding.textZoom.text = String.format("Zoom: %.1fx", zoomRatio)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startCamera()
    }

    private fun toggleFlash() {
        if (camera != null && camera!!.cameraInfo.hasFlashUnit()) {
            isFlashOn = !isFlashOn
            camera!!.cameraControl.enableTorch(isFlashOn)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
                
                // Initialize Zoom text
                val zoomState = camera?.cameraInfo?.zoomState
                zoomState?.observe(this) { state ->
                    // Optional: Sync seekbar if zoom changes externally (pinch)
                    // binding.textZoom.text = String.format("Zoom: %.1fx", state.zoomRatio)
                }

            } catch (exc: Exception) {
                Log.e("MagnifierActivity", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
