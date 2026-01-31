package com.dev.utilix

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.dev.utilix.databinding.ActivityQrScannerBinding
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var isScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnClose.setOnClickListener { finish() }

        startCamera()
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

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { result ->
                        if (!isScanned) {
                            isScanned = true
                            runOnUiThread {
                                val intent = android.content.Intent(this@QrScannerActivity, QrResultActivity::class.java)
                                intent.putExtra("SCANNED_TEXT", result)
                                startActivity(intent)
                                finish() // Stop scanning so we don't pile up activities
                            }
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("QrScanner", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // Inner class for analysis
    private class QrCodeAnalyzer(private val onQrFound: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val reader = MultiFormatReader()

        override fun analyze(image: ImageProxy) {
            try {
                // We only need the Y plane (Luminance)
                val plane = image.planes[0]
                val width = image.width
                val height = image.height
                val rowStride = plane.rowStride
                val buffer = plane.buffer

                // If the buffer size matches width*height, we can use it directly.
                // If rowStride > width, there is padding. We must strip it or handle it.
                // However, doing a byte-by-byte copy in Kotlin/Java is slow. 
                // A simple trick is to ignore the padding if we can, but ZXing expects a dense array.
                
                val data = ByteArray(width * height)
                
                // Copy data row by row to remove padding
                buffer.rewind()
                if (width == rowStride) {
                    buffer.get(data)
                } else {
                    for (y in 0 until height) {
                        buffer.position(y * rowStride)
                        buffer.get(data, y * width, width)
                    }
                }

                val source = PlanarYUVLuminanceSource(
                    data, width, height, 0, 0, width, height, false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                val result = reader.decode(binaryBitmap)
                onQrFound(result.text)
                
            } catch (e: Exception) {
                // NotFoundException, ChecksumException, FormatException are expected when no QR is found
                // We don't log them to keep logcat clean
            } finally {
                image.close()
            }
        }
    }
}
