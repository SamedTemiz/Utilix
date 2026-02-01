package com.dev.utilix

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.dev.utilix.R
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        
        // Apply language before inflating view? Usually needs restart.
        // For this simple implementation, we will restart app on language change.
        
        setContentView(R.layout.activity_settings)

        findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            finish()
        }

        setupTheme()
        setupLanguage()
        setupUnits()
        setupToggles()
    }

    private fun setupTheme() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupTheme)
        
        when(preferenceManager.theme) {
            "light" -> radioGroup.check(R.id.radioThemeLight)
            "dark" -> radioGroup.check(R.id.radioThemeDark)
            else -> radioGroup.check(R.id.radioThemeSystem)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when(checkedId) {
                R.id.radioThemeLight -> "light"
                R.id.radioThemeDark -> "dark"
                else -> "system"
            }
            
            if (themeMode != preferenceManager.theme) {
                preferenceManager.theme = themeMode
                applyTheme(themeMode)
            }
        }
    }

    private fun applyTheme(themeMode: String) {
        val mode = when(themeMode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        recreate()
    }

    private fun setupLanguage() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val currentLang = preferenceManager.language
        
        if (currentLang == "tr") {
            radioGroup.check(R.id.radioTurkish)
        } else {
            radioGroup.check(R.id.radioEnglish)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newLang = if (checkedId == R.id.radioTurkish) "tr" else "en"
            if (newLang != currentLang) {
                preferenceManager.language = newLang
                // Restart app to apply language
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun setupUnits() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupUnits)
        val currentUnit = preferenceManager.unit
        
        if (currentUnit == "imperial") {
            radioGroup.check(R.id.radioImperial)
        } else {
            radioGroup.check(R.id.radioMetric)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newUnit = if (checkedId == R.id.radioImperial) "imperial" else "metric"
            preferenceManager.unit = newUnit
        }
    }

    private fun setupToggles() {
        val switchHaptic = findViewById<SwitchMaterial>(R.id.switchHaptic)
        val switchSound = findViewById<SwitchMaterial>(R.id.switchSound)

        switchHaptic.isChecked = preferenceManager.isHapticEnabled
        switchSound.isChecked = preferenceManager.isSoundEnabled

        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.isHapticEnabled = isChecked
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.isSoundEnabled = isChecked
        }
    }
}
