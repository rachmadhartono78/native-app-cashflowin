package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityDebtsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DebtsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtsBinding
    // Format tanpa desimal agar ringkas
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    private lateinit var debtsAdapter: DebtsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        debtsAdapter = DebtsAdapter(emptyList()) { debt ->
            val intent = Intent(this, DebtDetailActivity::class.java)
            intent.putExtra("EXTRA_DEBT_ID", debt.id)
            startActivity(intent)
        }
        binding.rvDebts.apply {
            layoutManager = LinearLayoutManager(this@DebtsActivity)
            adapter = debtsAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddDebt.setOnClickListener {
            startActivity(Intent(this, AddDebtActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDebts()
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
                    debtsAdapter.updateData(data.debts)
                    
                } else {
                    showError("Gagal mengambil data hutang")
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
