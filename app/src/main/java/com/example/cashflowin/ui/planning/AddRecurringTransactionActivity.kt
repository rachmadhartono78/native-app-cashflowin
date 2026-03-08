package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.RecurringTransactionRequest
import com.example.cashflowin.databinding.ActivityAddRecurringTransactionBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddRecurringTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecurringTransactionBinding
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

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
        binding.etStartDate.setOnClickListener {
            showDatePicker { date ->
                binding.etStartDate.setText(displayDateFormat.format(date))
                binding.etStartDate.tag = apiDateFormat.format(date) // Store actual API format in tag
            }
        }

        binding.etEndDate.setOnClickListener {
            showDatePicker { date ->
                binding.etEndDate.setText(displayDateFormat.format(date))
                binding.etEndDate.tag = apiDateFormat.format(date)
            }
        }

        binding.btnSave.setOnClickListener {
            saveRecurringTransaction()
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // MaterialDatePicker returns time in UTC, convert to local
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            onDateSelected(calendar.time)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun saveRecurringTransaction() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val type = binding.spType.selectedItem.toString().lowercase()
        val frequency = binding.spFrequency.selectedItem.toString().lowercase()
        
        // Use tags to get the YYYY-MM-DD format for API
        val startDate = binding.etStartDate.tag?.toString() ?: ""
        val endDate = binding.etEndDate.tag?.toString()

        if (name.isEmpty() || amountStr.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field yang diperlukan", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RecurringTransactionRequest(
            name = name,
            description = description.ifEmpty { null },
            amount = amountStr.toDoubleOrNull() ?: 0.0,
            category_id = 1, // Placeholder
            asset_id = 1,    // Placeholder
            type = type,
            frequency = frequency,
            start_date = startDate,
            end_date = endDate?.ifEmpty { null },
            auto_execute = binding.cbAutoExecute.isChecked
        )

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddRecurringTransactionActivity)
                val response = apiService.addRecurringTransaction(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@AddRecurringTransactionActivity, "Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddRecurringTransactionActivity, "Gagal menambahkan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
