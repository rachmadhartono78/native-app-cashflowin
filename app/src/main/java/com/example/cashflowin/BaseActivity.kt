package com.example.cashflowin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.R
import com.example.cashflowin.utils.ThemeManager

/**
 * BaseActivity — superclass untuk semua Activity di app ini.
 *
 * Tujuan utama: memanggil ThemeManager.applyTheme() SEBELUM super.onCreate()
 * agar Material3 theme attributes (colorPrimary, colorSurface, dll) ter-apply
 * dengan benar pada saat layout di-inflate.
 *
 * Cara pakai: ubah setiap Activity yang extend AppCompatActivity
 * menjadi extend BaseActivity. Tidak perlu perubahan logika lain.
 *
 * Contoh:
 *   // Sebelum:
 *   class LoginActivity : AppCompatActivity() { ... }
 *
 *   // Sesudah:
 *   class LoginActivity : BaseActivity() { ... }
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ HARUS di sini, SEBELUM super.onCreate()
        // setTheme() hanya efektif jika dipanggil sebelum window dibuat.
        // Setelah super.onCreate(), theme sudah di-attach ke window dan tidak bisa diubah.
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)

        // Jika baru saja ada pergantian tema (flag one-shot dari ThemeManager),
        // terapkan animasi fade DAN switch launcher icon.
        //
        // ✅ Kenapa di sini (bukan di SettingsFragment)?
        //    PackageManager.setComponentEnabledSetting() kadang kill proses saat dipanggil
        //    dari Activity yang sedang mati (saat recreate() berjalan).
        //    Dengan memanggil dari Activity BARU yang sudah established, force close tidak terjadi.
        if (ThemeManager.consumeThemeChanged(this)) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.theme_fade_in, R.anim.theme_fade_out)

            // Switch launcher icon di Activity yang sudah fresh — aman dari force close
            ThemeManager.switchLauncherIcon(this, ThemeManager.getBrand(this))
        }
    }
}
