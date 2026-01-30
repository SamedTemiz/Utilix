package com.samed.utilitybelt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.samed.utilitybelt.databinding.ActivityQrResultBinding

class QrResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val scannedText = intent.getStringExtra("SCANNED_TEXT") ?: ""
        binding.textContent.text = scannedText

        // Check if Link
        if (Patterns.WEB_URL.matcher(scannedText).matches()) {
            binding.btnOpenLink.visibility = View.VISIBLE
            binding.btnOpenLink.setOnClickListener {
                try {
                    val url = if (!scannedText.startsWith("http://") && !scannedText.startsWith("https://")) {
                        "http://$scannedText"
                    } else {
                        scannedText
                    }
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("QR Code", scannedText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
