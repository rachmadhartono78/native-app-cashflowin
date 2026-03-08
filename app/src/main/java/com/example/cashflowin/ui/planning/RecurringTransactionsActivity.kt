package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.RecurringTransaction
import com.example.cashflowin.databinding.ActivityRecurringTransactionsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecurringTransactionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecurringTransactionsBinding
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
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
        recurringAdapter = RecurringTransactionAdapter(emptyList())
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
                    val data = response.body()!!.data
                    
                    if (data.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvRecurring.visibility = View.VISIBLE
                        recurringAdapter.updateData(data)
                    }
                    
                } else {
                    showError("Gagal mengambil data transaksi berulang")
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
