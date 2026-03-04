package com.example.cashflowin.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.DashboardRepository
import com.example.cashflowin.api.model.Summary
import com.example.cashflowin.databinding.FragmentDashboardBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.utils.TokenManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            requireActivity().application,
            DashboardRepository(ApiClient.getApiService(requireContext()))
        )
    }
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        if (tokenManager.getToken() == null) {
            navigateToLogin()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.fabAddTransaction.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java))
        }

        val calendar = Calendar.getInstance()
        val currentMonth = (calendar.get(Calendar.MONTH) + 1).toString()
        val currentYear = calendar.get(Calendar.YEAR).toString()

        binding.btnExportPdf.setOnClickListener {
            viewModel.exportReportPdf(currentMonth, currentYear) { responseBody ->
                saveFileToDownloads(responseBody, "Laporan_Keuangan.pdf")
            }
        }

        binding.btnExportCsv.setOnClickListener {
            viewModel.exportReportCsv(currentMonth, currentYear) { responseBody ->
                saveFileToDownloads(responseBody, "Laporan_Keuangan.csv")
            }
        }
    }

    private fun saveFileToDownloads(body: ResponseBody, fileName: String): Pair<Boolean, String> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val timestamp = System.currentTimeMillis()
            val finalFileName = fileName.replace(".pdf", "_${timestamp}.pdf").replace(".csv", "_${timestamp}.csv")
            val file = File(downloadsDir, finalFileName)
            
            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            Pair(true, "Saved: ${file.name}")
        } catch (e: Exception) {
            Pair(false, e.message ?: "Unknown IO error")
        }
    }

    override fun onResume() {
        super.onResume()
        if (tokenManager.getToken() != null) {
            viewModel.loadDashboardData()
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            val intent = Intent(requireContext(), com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java).apply {
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
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardState.Idle -> setLoading(false)
                is DashboardState.Loading -> setLoading(true)
                is DashboardState.Success -> {
                    setLoading(false)
                    val summary = state.response.data?.summary
                    val transactions = state.response.data?.recent_transactions ?: emptyList()

                    summary?.let { updateUI(it) }
                    transactionAdapter.submitList(transactions)
                    
                    binding.rvTransactions.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
                }
                is DashboardState.ExportComplete -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Success: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is DashboardState.Error -> {
                    setLoading(false)
                    if (state.message == "UNAUTHORIZED") {
                        tokenManager.clearToken()
                        navigateToLogin()
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {}
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnExportPdf.isEnabled = !isLoading
        binding.btnExportCsv.isEnabled = !isLoading
    }

    private fun updateUI(summary: Summary) {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalBalance.text = format.format(summary.balance)
        binding.tvIncome.text = format.format(summary.total_income_month)
        binding.tvExpense.text = format.format(summary.total_expense_month)
        
        setupPieChart(summary)
    }

    private fun setupPieChart(summary: Summary) {
        val entries = ArrayList<PieEntry>()
        if (summary.total_income_month > 0.0) {
            entries.add(PieEntry(summary.total_income_month.toFloat(), "Income"))
        }
        if (summary.total_expense_month > 0.0) {
            entries.add(PieEntry(summary.total_expense_month.toFloat(), "Expense"))
        }

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("No transactions this month.")
            binding.pieChart.clear()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf("#10B981".toColorInt(), "#EF4444".toColorInt())
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            centerText = "Month Overview"
            setUsePercentValues(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
