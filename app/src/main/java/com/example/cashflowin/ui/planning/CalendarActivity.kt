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
            // Kita buat string tanggal yang dipilih
            val selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            fetchTransactionsForDate(selectedDate)
        }
        
        // Load hari ini saat pertama kali buka
        val cal = Calendar.getInstance()
        val today = apiDateFormat.format(cal.time)
        fetchTransactionsForDate(today)
    }

    private fun fetchTransactionsForDate(dateStr: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTransactions.visibility = View.GONE
        
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
                
                // TRICK: Kita ambil range +/- 1 hari dari server untuk mengatasi masalah timezone,
                // tapi nanti kita filter lagi secara ketat di lokal.
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
                    
                    // FILTER KETAT: Bandingkan hanya bagian YYYY-MM-DD
                    val filtered = allTransactions.filter { 
                        it.date.startsWith(dateStr) 
                    }
                    
                    binding.tvSelectedDate.text = if (filtered.isEmpty()) {
                        "Tidak ada transaksi pada $displayDate"
                    } else {
                        "${filtered.size} Transaksi pada $displayDate"
                    }
                    
                    binding.rvTransactions.visibility = View.VISIBLE
                    transactionAdapter.submitList(filtered)
                    
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
