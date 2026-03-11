package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.BudgetRequest
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.databinding.ActivityAddBudgetBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.util.Calendar

class AddBudgetActivity : BaseActivity() {

    private lateinit var binding: ActivityAddBudgetBinding
    private var categoriesList: List<CategoryInfo> = emptyList()
    private var budgetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        budgetId = intent.getIntExtra("EXTRA_ID", -1)
        if (budgetId != -1) {
            binding.toolbar.title = "Edit Anggaran"
            binding.etAmount.setText(intent.getDoubleExtra("EXTRA_AMOUNT", 0.0).toLong().toString())
        }

        setupToolbar()
        setupListeners()
        fetchCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))
        binding.btnSave.setOnClickListener { saveBudget() }
    }

    private fun fetchCategories() {
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddBudgetActivity)
                val response = apiService.getCategories("expense")
                if (response.isSuccessful && response.body() != null) {
                    categoriesList = response.body()!!.data
                    val adapter = ArrayAdapter(
                        this@AddBudgetActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        categoriesList.map { it.name }
                    )
                    binding.spinnerCategory.adapter = adapter
                    
                    // Pre-select category if editing
                    if (budgetId != -1) {
                        val categoryId = intent.getIntExtra("EXTRA_CATEGORY_ID", -1)
                        val index = categoriesList.indexOfFirst { it.id == categoryId }
                        if (index != -1) {
                            binding.spinnerCategory.setSelection(index)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddBudgetActivity, "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBudget() {
        val amountFormatted = binding.etAmount.text.toString().trim()
        val amountStr = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toString()
        val selectedCategoryPos = binding.spinnerCategory.selectedItemPosition

        if (amountFormatted.isEmpty()) {
            binding.etAmount.error = "Jumlah anggaran diperlukan"
            return
        }
        if (selectedCategoryPos == -1 || categoriesList.isEmpty()) {
            Toast.makeText(this, "Silakan pilih kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val request = BudgetRequest(
            category_id = categoriesList[selectedCategoryPos].id,
            amount = amountStr.toDoubleOrNull() ?: 0.0,
            month = Calendar.getInstance().get(Calendar.MONTH) + 1,
            year = Calendar.getInstance().get(Calendar.YEAR)
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddBudgetActivity)
                // Backend might not have separate update endpoint for budgets in some designs, 
                // but usually POST to /budgets creates or updates for that month/category.
                // If there's no specific PUT /budgets/{id}, we'll use addBudget.
                val response = apiService.addBudget(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@AddBudgetActivity, "Anggaran berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddBudgetActivity, "Gagal menyimpan anggaran", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddBudgetActivity, "Kesalahan jaringan", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }
}