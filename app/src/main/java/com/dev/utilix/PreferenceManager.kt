package com.dev.utilix

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_UNIT = "unit" // "metric" or "imperial"
        const val KEY_HAPTIC = "haptic"
        const val KEY_SOUND = "sound"
    }

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var unit: String
        get() = prefs.getString(KEY_UNIT, "metric") ?: "metric"
        set(value) = prefs.edit().putString(KEY_UNIT, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()
}
