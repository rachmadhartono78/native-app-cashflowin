package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.Debt
import com.example.cashflowin.databinding.ActivityDebtDetailBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DebtDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtDetailBinding
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    private var debtId: Int = -1
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        debtId = intent.getIntExtra("EXTRA_DEBT_ID", -1)

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (debtId != -1) {
            fetchDebtDetails()
        } else {
            Toast.makeText(this, "ID Catatan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter(emptyList())
        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(this@DebtDetailActivity)
            adapter = paymentAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddPayment.setOnClickListener {
            val intent = android.content.Intent(this, AddPaymentActivity::class.java)
            intent.putExtra("EXTRA_DEBT_ID", debtId)
            
            val remainingText = binding.tvRemainingAmount.text.toString()
            val remaining = CurrencyTextWatcher.getUnformattedValue(remainingText)
            intent.putExtra("EXTRA_REMAINING_AMOUNT", remaining)
            
            startActivity(intent)
        }
    }

    private fun fetchDebtDetails() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentScroll.visibility = View.GONE
        binding.fabAddPayment.hide()

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@DebtDetailActivity)
                val response = apiService.getDebtDetails(debtId)

                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val debt = response.body()!!.data
                    bindData(debt)
                    binding.contentScroll.visibility = View.VISIBLE
                    if (debt.status == "pending") {
                        binding.fabAddPayment.show()
                    }
                } else {
                    Toast.makeText(this@DebtDetailActivity, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@DebtDetailActivity, "Kesalahan jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindData(debt: Debt) {
        binding.tvPersonName.text = debt.person_name
        
        val typeStr = if (debt.type == "debt") "Hutang Saya" else "Piutang (Dihutangi)"
        val statusStr = if (debt.status == "pending") "Belum Lunas" else "Lunas"
        binding.tvTypeAndStatus.text = "$typeStr • $statusStr"
        
        val remaining = debt.remaining_amount ?: debt.amount
        binding.tvRemainingAmount.text = currencyFormat.format(remaining)
        binding.tvTotalAmount.text = currencyFormat.format(debt.amount)
        
        // Format Tanggal Jatuh Tempo
        if (!debt.due_date.isNullOrEmpty()) {
            binding.tvDueDate.visibility = View.VISIBLE
            binding.tvDueDate.text = "Jatuh Tempo: ${formatDate(debt.due_date)}"
        } else {
            binding.tvDueDate.visibility = View.GONE
        }
        
        if (!debt.description.isNullOrEmpty()) {
            binding.tvDescription.visibility = View.VISIBLE
            binding.tvDescription.text = debt.description
        } else {
            binding.tvDescription.visibility = View.GONE
        }
        
        val payments = debt.payments
        if (payments.isNullOrEmpty()) {
            binding.tvNoPayments.visibility = View.VISIBLE
            binding.rvPayments.visibility = View.GONE
        } else {
            binding.tvNoPayments.visibility = View.GONE
            binding.rvPayments.visibility = View.VISIBLE
            paymentAdapter.updateData(payments)
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "-"
        
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        )
        
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        
        for (format in inputFormats) {
            try {
                if (format.toPattern().contains("Z")) {
                    format.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = format.parse(dateStr)
                if (date != null) return outputFormat.format(date)
            } catch (e: Exception) {
                continue
            }
        }
        return dateStr
    }
}
