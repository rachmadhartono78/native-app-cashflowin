package com.example.cashflowin.ui.planning

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.DebtRequest
import com.example.cashflowin.databinding.ActivityAddDebtBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDebtActivity : BaseActivity() {

    private lateinit var binding: ActivityAddDebtBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDebtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))
        
        binding.etDueDate.setOnClickListener {
            DatePickerDialog(
                this, { _, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    binding.etDueDate.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener { saveDebt() }
    }

    private fun saveDebt() {
        val name = binding.etName.text.toString().trim()
        val amountFormatted = binding.etAmount.text.toString().trim()
        val amountStr = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toString()
        val desc = binding.etDescription.text.toString().trim()
        
        var dueDateStr: String? = binding.etDueDate.text.toString().trim()
        if (dueDateStr != null && dueDateStr.isEmpty()) {
            dueDateStr = null
        }

        val type = if (binding.rbDebt.isChecked) "debt" else "receivable"

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }
        if (amountFormatted.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            return
        }

        val request = DebtRequest(
            person_name = name,
            type = type,
            amount = amountStr.toDoubleOrNull() ?: 0.0,
            due_date = dueDateStr,
            description = desc
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddDebtActivity)
                val response = apiService.addDebt(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddDebtActivity, "Catatan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddDebtActivity, "Gagal menyimpan catatan", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddDebtActivity, "Network error", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }
}