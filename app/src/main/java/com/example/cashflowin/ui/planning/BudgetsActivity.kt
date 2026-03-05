package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.Budget
import com.example.cashflowin.databinding.ActivityBudgetsBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetsBinding
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        fetchBudgets()
    }

    private fun setupListeners() {
        binding.fabAddBudget.setOnClickListener {
            startActivity(android.content.Intent(this, AddBudgetActivity::class.java))
        }
    }

    private fun fetchBudgets() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvBudgets.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@BudgetsActivity)
                val response = apiService.getBudgets()
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    
                    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                        Calendar.getInstance().apply {
                            set(Calendar.MONTH, data.month - 1)
                            set(Calendar.YEAR, data.year)
                        }.time
                    )
                    binding.tvMonthYear.text = monthName
                    binding.tvTotalBudget.text = format.format(data.total_budget)
                    binding.tvTotalSpent.text = "Spent: ${format.format(data.total_spent)}"
                    
                    binding.rvBudgets.visibility = View.VISIBLE
                    // TODO: Setup Adapter for `data.budgets`
                    
                } else {
                    showError("Failed to fetch budgets")
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
