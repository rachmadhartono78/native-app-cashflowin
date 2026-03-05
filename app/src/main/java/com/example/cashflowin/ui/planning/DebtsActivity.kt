package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityDebtsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DebtsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtsBinding
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        fetchDebts()
    }

    private fun setupListeners() {
        binding.fabAddDebt.setOnClickListener {
            startActivity(Intent(this, AddDebtActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDebts() // Refresh when returning
    }

    private fun fetchDebts() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvDebts.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@DebtsActivity)
                val response = apiService.getDebts()
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    
                    binding.tvTotalDebt.text = format.format(data.total_debt_balance)
                    binding.tvTotalReceivable.text = format.format(data.total_receivable_balance)
                    
                    binding.rvDebts.visibility = View.VISIBLE
                    
                    // TODO: Setup Adapter for `data.debts` and set click listener to go to DebtDetailActivity
                    // val adapter = DebtsAdapter(data.debts) { debt ->
                    //     val intent = Intent(this@DebtsActivity, DebtDetailActivity::class.java)
                    //     intent.putExtra("EXTRA_DEBT_ID", debt.id)
                    //     startActivity(intent)
                    // }
                    // binding.rvDebts.adapter = adapter
                    
                } else {
                    showError("Failed to fetch debts")
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
