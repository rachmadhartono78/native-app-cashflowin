package com.example.cashflowin

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.cashflowin.databinding.ActivityMainBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Paksa Edge-to-Edge dengan style transparan penuh
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        
        super.onCreate(savedInstanceState)

        // 2. Hilangkan proteksi contrast agar navigasi sistem benar-benar menyatu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navViewContainer.bringToFront()

        // 3. Update Insets: Kuncinya adalah integrasi navigasi bawah
        ViewCompat.setOnApplyWindowInsetsListener(binding.activityContainer) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // Toolbar tetap di bawah Status Bar
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            
            val density = resources.displayMetrics.density

            // INTEGRASI NAVIGASI: Buat nav bar menempel ke pinggir dan bawah
            binding.navViewContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = 0
                rightMargin = 0
                bottomMargin = 0
            }
            
            // PENTING: Berikan padding pada BottomNavigationView, bukan containernya
            // Padding atas (12dp) agar icon tidak mepet lengkungan atas
            // Padding bawah (navBars.bottom + 8dp) agar teks label tidak terpotong tombol sistem
            binding.navView.setPadding(
                0, 
                (12 * density).toInt(), 
                0, 
                navBars.bottom + (8 * density).toInt()
            )

            // Padding untuk konten fragment agar tidak tertutup bar navigasi
            // Estimasi tinggi: 64dp (minHeight) + 12dp (padding top) + 8dp (padding bottom) + navBars.bottom
            val totalBottomPadding = ((64 + 12 + 8) * density).toInt() + navBars.bottom
            binding.navHostFragmentActivityMain.setPadding(0, 0, 0, totalBottomPadding)

            insets
        }

        tokenManager = TokenManager(this)
        val token = tokenManager.getToken()

        if (token == null) {
            navigateToLogin()
            return
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_transactions, R.id.nav_categories, R.id.nav_assets, R.id.nav_settings
            )
        )
        
        binding.toolbar.let {
            setupActionBarWithNavControllerCustom(it, navController, appBarConfiguration)
        }
        
        navView.setupWithNavController(navController)
    }

    private fun setupActionBarWithNavControllerCustom(toolbar: androidx.appcompat.widget.Toolbar, navController: androidx.navigation.NavController, appBarConfiguration: AppBarConfiguration) {
        setSupportActionBar(toolbar)
        androidx.navigation.ui.NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
