package com.samed.utilitybelt

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast

class FeatureManager(private val context: Context) {

    private var isFlashOn = false
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // --- FLASHLIGHT ---
    fun toggleFlashlight(): Boolean {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Usually back camera
            isFlashOn = !isFlashOn
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, isFlashOn)
            }
            return isFlashOn
        } catch (e: Exception) {
            e.printStackTrace()
             Toast.makeText(context, "Flashlight Error: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    // --- VIBRATION ---
    fun toggleVibration(shouldVibrate: Boolean) {
        if (shouldVibrate) {
            // Vibrate indefinitely
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrateEffect = VibrationEffect.createWaveform(longArrayOf(0, 100, 50), 0) // 0 means repeat at index 0
                vibrator.vibrate(vibrateEffect)
            } else {
                 @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50), 0)
            }
        } else {
            vibrator.cancel()
        }
    }
    
    // --- HAPTIC FEEDBACK (Single Tick) ---
    fun hapticTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
             @Suppress("DEPRECATION")
            vibrator.vibrate(10) // 10ms for old devices
        }
    }

    // --- WATER EJECT (Sound) ---
    private var audioTrack: android.media.AudioTrack? = null
    private var isPlayingSound = false

    fun toggleWaterEject(): Boolean {
        if (isPlayingSound) {
            stopWaterEject()
            return false
        } else {
            startWaterEject()
            return true
        }
    }

    private fun startWaterEject() {
        Thread {
            try {
                val sampleRate = 44100
                val freqOfTone = 165.0 // Hz
                val duration = 10 // seconds (looping manually)
                val numSamples = duration * sampleRate
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                // Fill the array
                for (i in 0 until numSamples) {
                    sample[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (sampleRate / freqOfTone))
                }

                // Convert to 16 bit pcm sound array
                var idx = 0
                for (dVal in sample) {
                    val valShort = (dVal * 32767).toInt().toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) shr 8).toByte()
                }

                val bufferSize = android.media.AudioTrack.getMinBufferSize(
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.size.coerceAtLeast(bufferSize),
                    android.media.AudioTrack.MODE_STATIC
                )

                audioTrack?.write(generatedSnd, 0, generatedSnd.size)
                audioTrack?.play()
                isPlayingSound = true

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun stopWaterEject() {
        try {
            if (audioTrack != null) {
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
                isPlayingSound = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
    }
    }

    // --- SYSTEM UTILS (Media Mute) ---
    private var originalVolume = -1

    fun toggleMediaVolume(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)

        if (currentVolume > 0) {
            // Mute
            originalVolume = currentVolume // save for restore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_MUTE, 0)
            } else {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, 0, 0)
            }
            return false // Muted
        } else {
            // Unmute
            // If we have a saved volume, restore it. Otherwise max.
            val targetVol = if (originalVolume != -1) originalVolume else (maxVolume / 2)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_UNMUTE, 0)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVol, 0)
            } else {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVol, 0)
            }
            return true // Unmuted
        }
    }

    // --- DECISION MAKER ---
    fun rollDice(): Int {
        return (1..6).random()
    }

    fun flipCoin(): String {
        return if ((0..1).random() == 0) "HEADS" else "TAILS"
    }
}
