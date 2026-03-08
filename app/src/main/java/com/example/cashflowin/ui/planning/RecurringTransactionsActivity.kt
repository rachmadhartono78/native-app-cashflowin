package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityRecurringTransactionsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class RecurringTransactionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecurringTransactionsBinding
    private val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    private lateinit var recurringAdapter: RecurringTransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecurringTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchRecurringTransactions()
    }

    private fun setupRecyclerView() {
        recurringAdapter = RecurringTransactionAdapter(emptyList()) { transaction ->
            togglePauseResume(transaction)
        }
        binding.rvRecurring.apply {
            layoutManager = LinearLayoutManager(this@RecurringTransactionsActivity)
            adapter = recurringAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddRecurring.setOnClickListener {
            startActivity(android.content.Intent(this, AddRecurringTransactionActivity::class.java))
        }
    }

    private fun fetchRecurringTransactions() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvRecurring.visibility = View.GONE
        binding.emptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@RecurringTransactionsActivity)
                val response = apiService.getRecurringTransactions()
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data.data
                    
                    // Optimasi: Hitung hanya yang aktif untuk ringkasan
                    val activeTransactions = data.filter { it.is_active }
                    val activeCount = activeTransactions.size
                    
                    // Normalisasi estimasi ke bulanan
                    val totalMonthlyEstimation = activeTransactions.sumOf { 
                        when (it.frequency.lowercase()) {
                            "daily" -> it.amount * 30
                            "weekly" -> it.amount * 4
                            "monthly" -> it.amount
                            "yearly" -> it.amount / 12
                            else -> it.amount
                        }
                    }
                    
                    binding.tvSummaryCount.text = "$activeCount Transaksi Aktif"
                    binding.tvSummaryAmount.text = "Estimasi per bulan: ${format.format(totalMonthlyEstimation)}"
                    
                    if (data.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvRecurring.visibility = View.VISIBLE
                        recurringAdapter.updateData(data)
                    }
                    
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RecurringTransactions", "API Error: $errorBody")
                    showError("Gagal mengambil data: ${response.code()}")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("RecurringTransactions", "Network Error", e)
                showError("Kesalahan jaringan: ${e.localizedMessage}")
            }
        }
    }
    
    private fun togglePauseResume(transaction: com.example.cashflowin.api.model.RecurringTransaction) {
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@RecurringTransactionsActivity)
                val response = if (transaction.is_active) {
                    apiService.pauseRecurringTransaction(transaction.id)
                } else {
                    apiService.resumeRecurringTransaction(transaction.id)
                }

                if (response.isSuccessful) {
                    fetchRecurringTransactions()
                    val action = if (transaction.is_active) "dipause" else "dilanjutkan"
                    Toast.makeText(this@RecurringTransactionsActivity, "Transaksi berhasil $action", Toast.LENGTH_SHORT).show()
                } else {
                    showError("Gagal mengubah status")
                }
            } catch (e: Exception) {
                showError("Kesalahan: ${e.message}")
            }
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
