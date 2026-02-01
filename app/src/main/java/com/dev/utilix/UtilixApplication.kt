package com.dev.utilix

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class UtilixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Apply Theme Global Preference on Startup
        val preferenceManager = PreferenceManager(this)
        val mode = when(preferenceManager.theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
