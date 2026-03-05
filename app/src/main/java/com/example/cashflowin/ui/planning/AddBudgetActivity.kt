package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.BudgetRequest
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.databinding.ActivityAddBudgetBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.util.Calendar

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBudgetBinding
    private var categoriesList: List<CategoryInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.etAmount.error = "Amount is required"
            return
        }
        if (selectedCategoryPos == -1 || categoriesList.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
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
                val response = apiService.addBudget(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddBudgetActivity, "Budget saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddBudgetActivity, "Failed to save budget", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddBudgetActivity, "Network error", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }
}
