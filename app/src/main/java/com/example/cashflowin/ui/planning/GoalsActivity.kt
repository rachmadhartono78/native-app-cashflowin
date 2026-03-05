package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityGoalsBinding
import kotlinx.coroutines.launch

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        fetchGoals()
    }

    private fun setupListeners() {
        binding.fabAddGoal.setOnClickListener {
            startActivity(android.content.Intent(this, AddGoalActivity::class.java))
        }
    }

    private fun fetchGoals() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvGoals.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@GoalsActivity)
                val response = apiService.getGoals()
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    
                    binding.tvTotalGoals.text = "${data.size} Goals"
                    binding.rvGoals.visibility = View.VISIBLE
                    
                    // TODO: Setup Adapter for `data`
                    
                } else {
                    showError("Failed to fetch goals")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                showError("Network error: ${e.message}")
            }
        }
    }
    
    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
