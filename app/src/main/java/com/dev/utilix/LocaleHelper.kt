package com.dev.utilix

import android.content.Context
import android.content.ContextWrapper
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context): Context {
        val lang = PreferenceManager(context).language
        return setLocale(context, lang)
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
