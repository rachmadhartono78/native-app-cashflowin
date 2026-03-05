package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityGoalsBinding
import kotlinx.coroutines.launch

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding
    private lateinit var goalAdapter: GoalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchGoals()
    }

    private fun setupRecyclerView() {
        goalAdapter = GoalAdapter(emptyList()) { goal ->
            // Navigate to Goal Detail if you have one, or Edit Goal
            // For now, let's assume we might have a detail or just toast
            Toast.makeText(this, "Selected: ${goal.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvGoals.apply {
            layoutManager = LinearLayoutManager(this@GoalsActivity)
            adapter = goalAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddGoal.setOnClickListener {
            startActivity(Intent(this, AddGoalActivity::class.java))
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
                    goalAdapter.updateData(data)
                    
                } else {
                    showError("Gagal mengambil data target")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                showError("Kesalahan jaringan: ${e.message}")
            }
        }
    }
    
    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
