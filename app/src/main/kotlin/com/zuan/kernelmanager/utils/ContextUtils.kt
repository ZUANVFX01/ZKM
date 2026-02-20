/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.zuan.kernelmanager.ui.settings.SettingsPreference

object ContextUtils {

    fun updateBaseContext(context: Context): Context {
        // 1. Pastikan Locale tersimpan di AppCompatDelegate (Backup safety)
        val prefs = SettingsPreference.getInstance(context)
        val localeCode = prefs.currentLanguageCode.value
        
        // Hanya set jika belum sesuai, biar gak loop
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (localeCode != "system" && currentTags != localeCode) {
            val localeList = LocaleListCompat.forLanguageTags(localeCode)
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        // 2. LOGIKA DPI
        // Baca SharedPrefs manual (karena StateFlow mungkin belum siap di tahap awal context)
        val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val dpi = sharedPrefs.getInt("app_custom_dpi", 0)

        // Jika DPI 0, kembalikan context asli (ikut sistem)
        if (dpi == 0) return context

        // 3. Buat Konfigurasi Baru
        val configuration = Configuration(context.resources.configuration)
        configuration.densityDpi = dpi // Override DPI
        
        // 4. Kembalikan Context dengan konfigurasi yang sudah dimodifikasi
        return context.createConfigurationContext(configuration)
    }
}
