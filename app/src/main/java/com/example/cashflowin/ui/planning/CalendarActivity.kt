package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityCalendarBinding
import com.example.cashflowin.ui.dashboard.TransactionAdapter
import com.example.cashflowin.ui.transaction.AddTransactionActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
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
        setupEmptyState()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Detail transaksi
        }
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            fetchTransactionsForDate(selectedDate)
        }
        
        val cal = Calendar.getInstance()
        val today = apiDateFormat.format(cal.time)
        fetchTransactionsForDate(today)
    }

    private fun setupEmptyState() {
        binding.btnEmptyAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchTransactionsForDate(dateStr: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTransactions.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        
        val displayDate = try {
            val date = apiDateFormat.parse(dateStr)
            if (date != null) displayDateFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }

        binding.tvSelectedDate.text = "Mencari transaksi $displayDate..."

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@CalendarActivity)
                
                val cal = Calendar.getInstance()
                val date = apiDateFormat.parse(dateStr)
                cal.time = date!!
                
                cal.add(Calendar.DAY_OF_MONTH, -1)
                val prevDay = apiDateFormat.format(cal.time)
                
                cal.add(Calendar.DAY_OF_MONTH, 2)
                val nextDay = apiDateFormat.format(cal.time)

                val response = apiService.getTransactions(
                    startDate = prevDay,
                    endDate = nextDay
                )
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val allTransactions = response.body()?.data?.data ?: emptyList()
                    
                    val filtered = allTransactions.filter { 
                        it.date.startsWith(dateStr) 
                    }
                    
                    if (filtered.isEmpty()) {
                        binding.tvSelectedDate.text = "Belum ada catatan keuangan"
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvTransactions.visibility = View.GONE
                    } else {
                        binding.tvSelectedDate.text = "$displayDate (${filtered.size} Transaksi)"
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvTransactions.visibility = View.VISIBLE
                        transactionAdapter.submitList(filtered)
                    }
                    
                } else {
                    binding.tvSelectedDate.text = "Gagal memuat data"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvSelectedDate.text = "Kesalahan koneksi"
                Toast.makeText(this@CalendarActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
