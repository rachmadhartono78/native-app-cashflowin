package com.example.cashflowin.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.cashflowin.R

/**
 * ThemeManager — Singleton untuk mengelola brand color pilihan user.
 */
object ThemeManager {

    const val BRAND_GREEN  = "green"
    const val BRAND_INDIGO = "indigo"

    private const val PREF_NAME         = "cashflowin_theme_prefs"
    private const val KEY_BRAND_COLOR   = "brand_color"
    private const val KEY_THEME_CHANGED = "theme_just_changed"

    // ─── Konstanta Activity-Alias (HARUS sama dengan AndroidManifest.xml) ─────
    private const val ALIAS_GREEN  = "com.example.cashflowin.AliasCashflowinGreen"
    private const val ALIAS_INDIGO = "com.example.cashflowin.AliasCashflowinIndigo"

    fun saveBrand(context: Context, brand: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BRAND_COLOR, brand)
            .putBoolean(KEY_THEME_CHANGED, true)
            .apply()
    }

    fun getBrand(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BRAND_COLOR, BRAND_GREEN) ?: BRAND_GREEN

    fun applyTheme(context: Context) {
        val theme = when (getBrand(context)) {
            BRAND_INDIGO -> R.style.Theme_Cashflowin_Indigo
            else         -> R.style.Theme_Cashflowin
        }
        context.setTheme(theme)
    }

    fun consumeThemeChanged(context: Context): Boolean {
        val prefs   = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val changed = prefs.getBoolean(KEY_THEME_CHANGED, false)
        if (changed) prefs.edit().putBoolean(KEY_THEME_CHANGED, false).apply()
        return changed
    }

    /**
     * Switch launcher icon via activity-alias.
     */
    fun switchLauncherIcon(context: Context, brand: String) {
        val pkg = context.packageName
        
        val enableAlias  = if (brand == BRAND_INDIGO) ALIAS_INDIGO else ALIAS_GREEN
        val disableAlias = if (brand == BRAND_INDIGO) ALIAS_GREEN  else ALIAS_INDIGO

        val pm = context.packageManager

        try {
            // Enable yang baru
            pm.setComponentEnabledSetting(
                ComponentName(pkg, enableAlias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            // Disable yang lama
            pm.setComponentEnabledSetting(
                ComponentName(pkg, disableAlias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            android.util.Log.d("ThemeManager", "Icon switched to: $brand ($enableAlias)")
        } catch (e: Exception) {
            android.util.Log.e("ThemeManager", "Gagal switch icon: ${e.message}")
        }
    }

    fun isIndigoActive(context: Context): Boolean = getBrand(context) == BRAND_INDIGO
}