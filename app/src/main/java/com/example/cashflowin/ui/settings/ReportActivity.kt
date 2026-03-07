package com.example.cashflowin.ui.settings

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.databinding.ActivityReportBinding
import com.example.cashflowin.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var tokenManager: TokenManager

    private val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupToolbar()
        setupDropdowns()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdowns() {
        // Setup Months
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, months)
        binding.spinnerMonth.setAdapter(monthAdapter)

        // Setup Years (5 years back and 1 year ahead)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 1).map { it.toString() }.reversed()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, years)
        binding.spinnerYear.setAdapter(yearAdapter)

        // Set current month and year as default
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        binding.spinnerMonth.setText(months[currentMonth], false)
        binding.spinnerYear.setText(currentYear.toString(), false)
    }

    private fun setupListeners() {
        binding.btnDownload.setOnClickListener {
            val monthName = binding.spinnerMonth.text.toString()
            val monthIndex = months.indexOf(monthName) + 1
            val year = binding.spinnerYear.text.toString()
            val format = if (binding.rbPdf.isChecked) "pdf" else "csv"

            if (monthIndex == 0 || year.isEmpty()) {
                Toast.makeText(this, "Mohon pilih bulan dan tahun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            downloadReport(monthIndex.toString(), year, format)
        }
    }

    private fun downloadReport(month: String, year: String, format: String) {
        val baseUrl = ApiClient.BASE_URL
        val endpoint = if (format == "pdf") "reports/export/pdf" else "reports/export/csv"
        val url = "${baseUrl}${endpoint}?month=$month&year=$year"
        val token = tokenManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnDownload.isEnabled = false

        try {
            val fileName = "Laporan_Keuangan_${month}_${year}.${format}"
            
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Mengunduh Laporan")
                .setDescription("Sedang mengunduh $fileName")
                .addRequestHeader("Authorization", "Bearer $token")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "Unduhan dimulai. Cek panel notifikasi.", Toast.LENGTH_LONG).show()
            
            // Re-enable after a short delay
            lifecycleScope.launch {
                kotlinx.coroutines.delay(2000)
                binding.progressBar.visibility = View.GONE
                binding.btnDownload.isEnabled = true
            }

        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.btnDownload.isEnabled = true
            Toast.makeText(this, "Gagal mengunduh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
