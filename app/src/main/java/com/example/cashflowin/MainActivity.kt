package com.example.cashflowin

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.databinding.ActivityMainBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.utils.ThemeManager
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Enable Edge-to-Edge modern
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pastikan navigasi bar tidak memiliki kontras paksaan agar benar-benar transparan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setupWindowInsets()
        
        tokenManager = TokenManager(this)
        if (tokenManager.getToken() == null) {
            navigateToLogin()
            return
        }

        setupNavigation()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Terapkan padding atas untuk AppBarLayout (Status Bar)
            binding.appBarLayout.updatePadding(top = systemBars.top)
            
            // Terapkan padding bawah untuk NavView (Navigation Bar)
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // Update padding bawah BottomNav agar icon/label tidak tertutup navigation bar sistem
            binding.navView.updatePadding(bottom = navBars.bottom)
            
            // Pastikan FragmentContainerView memiliki padding bawah yang cukup agar konten tidak tertutup BottomNav
            // Kita gunakan post {} agar tinggi navView sudah terukur dengan benar
            binding.navViewContainer.post {
                val navHeight = binding.navViewContainer.height
                binding.navHostFragmentActivityMain.updatePadding(bottom = navHeight)
            }

            insets
        }
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
        
        setSupportActionBar(binding.toolbar)
        androidx.navigation.ui.NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Apply warna bottom navigation sesuai brand color aktif
        applyNavColors(navView)
    }

    /**
     * Pilih color state list yang tepat untuk BottomNavigationView.
     * Color state list di XML tidak bisa resolve ?attr/colorPrimary,
     * jadi kita terapkan secara programmatic di sini berdasarkan brand.
     */
    private fun applyNavColors(navView: BottomNavigationView) {
        val selectorRes = when (ThemeManager.getBrand(this)) {
            ThemeManager.BRAND_INDIGO -> R.color.bottom_nav_selector_indigo
            else                      -> R.color.bottom_nav_selector
        }
        val colorStateList = ContextCompat.getColorStateList(this, selectorRes)
        navView.itemIconTintList = colorStateList
        navView.itemTextColor   = colorStateList
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
