package com.example.cashflowin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Get NavHostFragment and NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_transactions, R.id.nav_categories, R.id.nav_assets, R.id.nav_settings
            )
        )
        
        // Ensure toolbar doesn't crash if it's null, but we do have one
        binding.toolbar?.let {
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
