package com.example.cashflowin.ui.planning

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.DebtPaymentRequest
import com.example.cashflowin.databinding.ActivityAddPaymentBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentBinding
    private val calendar = Calendar.getInstance()
    private var debtId: Int = -1
    private var remainingAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        debtId = intent.getIntExtra("EXTRA_DEBT_ID", -1)
        remainingAmount = intent.getDoubleExtra("EXTRA_REMAINING_AMOUNT", 0.0)

        setupToolbar()
        setupListeners()
        
        binding.tvRemainingAmount.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(remainingAmount)
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        binding.etPaymentDate.setText(sdf.format(calendar.time)) // Default to today
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))
        
        binding.etPaymentDate.setOnClickListener {
            DatePickerDialog(
                this, { _, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    binding.etPaymentDate.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener { performPayment() }
    }

    private fun performPayment() {
        if (debtId == -1) {
            Toast.makeText(this, "Invalid Debt ID", Toast.LENGTH_SHORT).show()
            return
        }

        val amountStr = CurrencyTextWatcher.getUnformattedValue(binding.etAmount.text.toString()).toString()
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val paymentDate = binding.etPaymentDate.text.toString()
        val assetId = binding.etAssetId.text.toString().toIntOrNull() ?: 1
        val categoryId = binding.etCategoryId.text.toString().toIntOrNull() ?: 1
        val notes = binding.etNotes.text.toString().trim()

        if (amount <= 0 || amount > remainingAmount) {
            binding.etAmount.error = "Jumlah tidak valid (Maksimal sisa hutang)"
            return
        }

        val request = DebtPaymentRequest(
            amount = amount,
            payment_date = paymentDate,
            asset_id = assetId,
            category_id = categoryId,
            notes = if (notes.isNotEmpty()) notes else null
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddPaymentActivity)
                val response = apiService.addDebtPayment(debtId, request)
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    Toast.makeText(this@AddPaymentActivity, "Pembayaran berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddPaymentActivity, "Gagal menyimpan pembayaran", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this@AddPaymentActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
