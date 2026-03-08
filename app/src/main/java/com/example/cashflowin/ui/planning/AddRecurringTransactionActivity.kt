package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.RecurringTransactionRequest
import com.example.cashflowin.databinding.ActivityAddRecurringTransactionBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddRecurringTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecurringTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecurringTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveRecurringTransaction()
        }
    }

    private fun saveRecurringTransaction() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val type = binding.spType.selectedItem.toString()
        val frequency = binding.spFrequency.selectedItem.toString()
        val startDate = binding.etStartDate.text.toString().trim()

        if (name.isEmpty() || amountStr.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field yang diperlukan", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RecurringTransactionRequest(
            name = name,
            description = description.ifEmpty { null },
            amount = amountStr.toDoubleOrNull() ?: 0.0,
            category_id = 1, // Placeholder - should be selected from dropdown
            asset_id = 1,    // Placeholder - should be selected from dropdown
            type = type,
            frequency = frequency,
            start_date = startDate,
            end_date = binding.etEndDate.text.toString().trim().ifEmpty { null },
            auto_execute = binding.cbAutoExecute.isChecked
        )

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddRecurringTransactionActivity)
                val response = apiService.addRecurringTransaction(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@AddRecurringTransactionActivity, "Transaksi berulang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddRecurringTransactionActivity, "Gagal menambahkan transaksi berulang", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
