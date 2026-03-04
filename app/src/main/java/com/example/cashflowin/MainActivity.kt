package com.example.cashflowin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.databinding.ActivityMainBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.ui.dashboard.DashboardState
import com.example.cashflowin.ui.dashboard.DashboardViewModel
import com.example.cashflowin.ui.dashboard.TransactionAdapter
import com.example.cashflowin.utils.TokenManager
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar
import java.io.File
import java.io.FileOutputStream
import android.os.Environment
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.example.cashflowin.api.model.Summary
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.ResponseBody

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        val token = tokenManager.getToken()

        if (token == null) {
            navigateToLogin()
            return
        }

        setupRecyclerView()
        setupToolbar()
        setupObservers()

        binding.fabAddTransaction?.setOnClickListener {
            startActivity(Intent(this, com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java))
        }

        setupExportButtons()
    }

    private fun setupExportButtons() {
        val calendar = Calendar.getInstance()
        val currentMonth = (calendar.get(Calendar.MONTH) + 1).toString()
        val currentYear = calendar.get(Calendar.YEAR).toString()

        binding.btnExportPdf?.setOnClickListener {
            val token = tokenManager.getToken() ?: return@setOnClickListener
            viewModel.exportReportPdf(token, currentMonth, currentYear) { responseBody ->
                saveFileToDownloads(responseBody, "Laporan_Keuangan.pdf")
            }
        }

        binding.btnExportCsv?.setOnClickListener {
            val token = tokenManager.getToken() ?: return@setOnClickListener
            viewModel.exportReportCsv(token, currentMonth, currentYear) { responseBody ->
                saveFileToDownloads(responseBody, "Laporan_Keuangan.csv")
            }
        }
    }

    private fun saveFileToDownloads(body: ResponseBody, fileName: String): Pair<Boolean, String> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // To ensure uniqueness and not overwriting, append timestamp
            val timestamp = System.currentTimeMillis()
            val finalFileName = fileName.replace(".pdf", "_${timestamp}.pdf").replace(".csv", "_${timestamp}.csv")
            val file = File(downloadsDir, finalFileName)
            
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            Pair(true, "Saved: ${file.absolutePath}")
        } catch (e: Exception) {
            Pair(false, e.message ?: "Unknown IO error")
        }
    }

    override fun onResume() {
        super.onResume()
        val token = tokenManager.getToken()
        if (token != null) {
            viewModel.loadDashboardData(token)
        }
    }

    private fun setupToolbar() {
        binding.toolbar?.inflateMenu(R.menu.main_menu)
        binding.toolbar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    val token = tokenManager.getToken()
                    if (token != null) {
                        viewModel.logout(token)
                    } else {
                        navigateToLogin()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            val intent = Intent(this, com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java).apply {
                putExtra("EXTRA_ID", transaction.id)
                putExtra("EXTRA_AMOUNT", transaction.amount)
                putExtra("EXTRA_TYPE", transaction.type)
                putExtra("EXTRA_DESC", transaction.description)
                putExtra("EXTRA_DATE", transaction.date)
                putExtra("EXTRA_CATEGORY_ID", transaction.category?.id)
                putExtra("EXTRA_ASSET_ID", transaction.asset?.id)
            }
            startActivity(intent)
        }
        binding.rvTransactions?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupObservers() {
        viewModel.dashboardState.observe(this) { state ->
            when (state) {
                is DashboardState.Idle -> {}
                is DashboardState.Loading -> {
                    binding.progressBar?.visibility = View.VISIBLE
                }
                is DashboardState.Success -> {
                    binding.progressBar?.visibility = View.GONE
                    val summary = state.response.data?.summary
                    val transactions = state.response.data?.recent_transactions ?: emptyList()

                    summary?.let {
                        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        binding.tvTotalBalance?.text = format.format(it.balance)
                        binding.tvIncome?.text = format.format(it.total_income_month)
                        binding.tvExpense?.text = format.format(it.total_expense_month)
                        
                        setupPieChart(it)
                    }

                    transactionAdapter.submitList(transactions)
                }
                is DashboardState.ExportComplete -> {
                    binding.progressBar?.visibility = View.GONE
                    Toast.makeText(this, "Success: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is DashboardState.Error -> {
                    binding.progressBar?.visibility = View.GONE
                    if (state.message == "UNAUTHORIZED" || state.message.contains("401")) {
                        Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                        tokenManager.clearToken()
                        navigateToLogin()
                    } else {
                        Toast.makeText(this, "Logout Failed: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                is DashboardState.LoggedOut -> {
                    binding.progressBar?.visibility = View.GONE
                    tokenManager.clearToken()
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            }
        }
    }

    private fun setupPieChart(summary: Summary) {
        val pieChart = binding.pieChart ?: return

        val entries = ArrayList<PieEntry>()
        if (summary.total_income_month > 0.0) {
            entries.add(PieEntry(summary.total_income_month.toFloat(), "Income"))
        }
        if (summary.total_expense_month > 0.0) {
            entries.add(PieEntry(summary.total_expense_month.toFloat(), "Expense"))
        }

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No transactions this month.")
            pieChart.clear()
            return
        }

        val dataSet = PieDataSet(entries, "")
        val colors = ArrayList<Int>()
        colors.add("#10B981".toColorInt()) // Green for income
        colors.add("#EF4444".toColorInt()) // Red for expense
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "This Month"
        pieChart.setUsePercentValues(true)
        pieChart.animateY(1000)
        pieChart.invalidate() // Refresh
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
