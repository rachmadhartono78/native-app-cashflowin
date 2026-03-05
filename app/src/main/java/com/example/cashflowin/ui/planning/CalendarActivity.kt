package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityCalendarBinding
import com.example.cashflowin.ui.dashboard.TransactionAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupCalendar()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Optionally handle click
        }
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = sdf.format(calendar.time)
            
            fetchTransactionsForDate(selectedDate)
        }
        
        // Fetch for today initially
        fetchTransactionsForDate(sdf.format(Calendar.getInstance().time))
    }

    private fun fetchTransactionsForDate(dateStr: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTransactions.visibility = View.GONE

        val parts = dateStr.split("-")
        if (parts.size < 3) return
        val year = parts[0]
        val month = parts[1]

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@CalendarActivity)
                
                // Fetch month's transactions
                val response = apiService.getTransactions(
                    startDate = "$year-$month-01",
                    endDate = "$year-$month-31"
                )
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val allTransactions = response.body()?.data?.data ?: emptyList()
                    val filtered = allTransactions.filter { it.date == dateStr }
                    
                    binding.tvSelectedDate.text = if (filtered.isEmpty()) {
                        "Tidak ada transaksi pada $dateStr"
                    } else {
                        "${filtered.size} Transaksi pada $dateStr"
                    }
                    
                    binding.rvTransactions.visibility = View.VISIBLE
                    transactionAdapter.submitList(filtered)
                    
                } else {
                    Toast.makeText(this@CalendarActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@CalendarActivity, "Kesalahan jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
