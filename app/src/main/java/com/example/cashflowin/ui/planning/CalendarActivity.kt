package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityCalendarBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupCalendar()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = sdf.format(calendar.time)
            binding.tvSelectedDate.text = "Transactions for $selectedDate"
            
            fetchTransactionsForDate(selectedDate)
        }
        
        // Fetch for today initially
        fetchTransactionsForDate(sdf.format(Calendar.getInstance().time))
    }

    private fun fetchTransactionsForDate(dateStr: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTransactions.visibility = View.GONE

        // Assuming dateStr format is "yyyy-MM-dd"
        val parts = dateStr.split("-")
        if (parts.size < 3) return
        val year = parts[0]
        val month = parts[1]

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@CalendarActivity)
                
                // Fetch all transactions for the month
                val response = apiService.getTransactions(
                    startDate = "$year-$month-01",
                    endDate = "$year-$month-31" // Simple boundary, backend date filter handles it
                )
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val allTransactions = response.body()?.data?.data ?: emptyList()
                    
                    // Filter matching the specific selected day
                    val filtered = allTransactions.filter { it.date == dateStr }
                    
                    if (filtered.isEmpty()) {
                        binding.tvSelectedDate.text = "No transactions on $dateStr"
                    } else {
                        binding.tvSelectedDate.text = "${filtered.size} Transactions on $dateStr"
                    }
                    
                    binding.rvTransactions.visibility = View.VISIBLE
                    // TODO: Create a simple TransactionAdapter or reuse existing one to display `filtered`
                    // binding.rvTransactions.adapter = TransactionListAdapter(filtered)
                    
                } else {
                    Toast.makeText(this@CalendarActivity, "Failed to fetch transactions", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@CalendarActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
