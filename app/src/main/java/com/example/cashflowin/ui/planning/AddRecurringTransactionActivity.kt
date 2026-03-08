package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.RecurringTransactionRequest
import com.example.cashflowin.databinding.ActivityAddRecurringTransactionBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
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
        // Parsing Angka Otomatis (Rp 1.000.000)
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))

        binding.etStartDate.setOnClickListener {
            showDatePicker { date ->
                binding.etStartDate.setText(displayDateFormat.format(date))
                binding.etStartDate.tag = apiDateFormat.format(date)
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
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            onDateSelected(calendar.time)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun saveRecurringTransaction() {
        val nameInput = binding.etName.text.toString().trim()
        val descInput = binding.etDescription.text.toString().trim()
        
        // Gabungkan name dan description karena backend hanya punya kolom description
        val finalDescription = if (descInput.isNotEmpty()) {
            "$nameInput - $descInput"
        } else {
            nameInput
        }
        
        // Ambil nilai asli tanpa titik pemisah ribuan
        val amountFormatted = binding.etAmount.text.toString().trim()
        val amountStr = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toString()
        
        val type = binding.spType.selectedItem.toString().lowercase()
        val frequency = binding.spFrequency.selectedItem.toString().lowercase()
        val startDate = binding.etStartDate.tag?.toString() ?: ""
        val endDate = binding.etEndDate.tag?.toString()

        if (nameInput.isEmpty()) {
            binding.etName.error = "Nama wajib diisi"
            return
        }
        if (amountFormatted.isEmpty() || amountStr == "0") {
            binding.etAmount.error = "Jumlah wajib diisi"
            return
        }
        if (startDate.isEmpty()) {
            Toast.makeText(this, "Pilih tanggal mulai", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RecurringTransactionRequest(
            description = finalDescription,
            amount = amountStr.toDoubleOrNull() ?: 0.0,
            category_id = 1, // Placeholder
            asset_id = 1,    // Placeholder
            type = type,
            frequency = frequency,
            frequency_interval = 1,
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
                    Toast.makeText(this@AddRecurringTransactionActivity, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
