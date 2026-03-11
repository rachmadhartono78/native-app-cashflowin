package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.BaseActivity
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

class AddRecurringTransactionActivity : BaseActivity() {

    private lateinit var binding: ActivityAddRecurringTransactionBinding
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    
    private var isEdit = false
    private var transactionId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecurringTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        if (isEdit && transactionId != -1) {
            supportActionBar?.title = "Edit Recurring Transaction"
            binding.btnSave.text = "Simpan Perubahan"
            fetchTransactionDetails()
        }

        setupListeners()
    }

    private fun fetchTransactionDetails() {
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddRecurringTransactionActivity)
                val response = apiService.getRecurringTransactionDetails(transactionId)
                if (response.isSuccessful && response.body() != null) {
                    val transaction = response.body()!!.data
                    populateData(transaction)
                } else {
                    Toast.makeText(this@AddRecurringTransactionActivity, "Gagal meload data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Kesalahan jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateData(transaction: com.example.cashflowin.api.model.RecurringTransaction) {
        val descParts = transaction.description?.split(" - ", limit = 2)
        val name = descParts?.getOrNull(0) ?: ""
        val desc = descParts?.getOrNull(1) ?: ""

        binding.etName.setText(name)
        binding.etDescription.setText(desc)
        binding.etAmount.setText(transaction.amount.toLong().toString()) // TextWatcher will format this
        
        // Select Type
        for (i in 0 until binding.spType.count) {
            if (binding.spType.getItemAtPosition(i).toString().equals(transaction.type, ignoreCase = true)) {
                binding.spType.setSelection(i)
                break
            }
        }

        // Select Frequency
        for (i in 0 until binding.spFrequency.count) {
            if (binding.spFrequency.getItemAtPosition(i).toString().equals(transaction.frequency, ignoreCase = true)) {
                binding.spFrequency.setSelection(i)
                break
            }
        }

        binding.cbAutoExecute.isChecked = transaction.auto_execute

        try {
            val sDateParsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(transaction.start_date.replace("T"," ").replace("Z",""))
            if(sDateParsed != null) {
                binding.etStartDate.setText(displayDateFormat.format(sDateParsed))
                binding.etStartDate.tag = apiDateFormat.format(sDateParsed)
            }
        } catch (e: Exception) {
            binding.etStartDate.setText(transaction.start_date)
            binding.etStartDate.tag = transaction.start_date
        }

        if (transaction.end_date != null) {
            try {
                val eDateParsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(transaction.end_date.replace("T"," ").replace("Z",""))
                if(eDateParsed != null) {
                   binding.etEndDate.setText(displayDateFormat.format(eDateParsed))
                   binding.etEndDate.tag = apiDateFormat.format(eDateParsed)
                }
            } catch (e: Exception) {
                binding.etEndDate.setText(transaction.end_date)
                binding.etEndDate.tag = transaction.end_date
            }
        }
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
                val response = if (isEdit) {
                    apiService.updateRecurringTransaction(transactionId, request)
                } else {
                    apiService.addRecurringTransaction(request)
                }

                if (response.isSuccessful) {
                    val msg = if (isEdit) "Berhasil diperbarui" else "Berhasil ditambahkan"
                    Toast.makeText(this@AddRecurringTransactionActivity, msg, Toast.LENGTH_SHORT).show()
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
