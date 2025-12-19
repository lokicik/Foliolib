package com.foliolib.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FolioApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("Foliolib application started")

        // Log the current application locale
        val appLocales = AppCompatDelegate.getApplicationLocales()
        Timber.d("FolioApplication onCreate - Application locales: $appLocales")
        if (!appLocales.isEmpty) {
            Timber.d("FolioApplication onCreate - Current locale: ${appLocales[0]}")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Timber.d("FolioApplication onConfigurationChanged - New locale: ${newConfig.locales[0]}")
    }
}
