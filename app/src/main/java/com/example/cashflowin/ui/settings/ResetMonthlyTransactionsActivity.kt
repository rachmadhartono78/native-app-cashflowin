package com.example.cashflowin.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.ResetMonthlyTransactionRequest
import com.example.cashflowin.databinding.ActivityResetMonthlyTransactionsBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class ResetMonthlyTransactionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetMonthlyTransactionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetMonthlyTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSpinners()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSpinners() {
        // Setup Month Spinner
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, months)
        binding.spinnerMonth.setAdapter(monthAdapter)

        // Pre-select current month
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        binding.spinnerMonth.setText(months[currentMonth], false)

        // Setup Year Spinner
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2000..2050).map { it.toString() }.toTypedArray()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, years)
        binding.spinnerYear.setAdapter(yearAdapter)

        // Pre-select current year
        binding.spinnerYear.setText(currentYear.toString(), false)
    }

    private fun setupListeners() {
        binding.btnReset.setOnClickListener {
            val password = binding.etPassword.text.toString().trim()
            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                return@setOnClickListener
            }

            // Get selected month index (1-12)
            val monthString = binding.spinnerMonth.text.toString()
            val months = arrayOf(
                "January", "February", "March", "April", "May", "June", 
                "July", "August", "September", "October", "November", "December"
            )
            val monthIndex = months.indexOf(monthString) + 1

            // Get selected year
            val yearString = binding.spinnerYear.text.toString()
            val year = yearString.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

            showConfirmationDialog(monthIndex, year, password)
        }
    }

    private fun showConfirmationDialog(month: Int, year: Int, password: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you absolutely sure you want to delete all transactions for ${month}/${year}? This action cannot be undone and will recalculate your asset balances.")
            .setPositiveButton("Delete", { _, _ ->
                performReset(month, year, password)
            })
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performReset(month: Int, year: Int, password: String) {
        setLoading(true)

        val request = ResetMonthlyTransactionRequest(month, year, password)
        val apiService = ApiClient.getApiService(this)

        lifecycleScope.launch {
            try {
                val response = apiService.resetMonthlyTransactions(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(
                        this@ResetMonthlyTransactionsActivity,
                        response.body()?.message ?: "Transactions successfully deleted.",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Set Result OK to trigger refresh if launched for result
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMessage = if (response.code() == 401) {
                        "Incorrect password. Please try again."
                    } else {
                        "Failed to delete transactions. Check your inputs."
                    }
                    Toast.makeText(this@ResetMonthlyTransactionsActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ResetMonthlyTransactionsActivity, 
                    "Network error: ${e.localizedMessage}", 
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnReset.text = ""
            binding.btnReset.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btnReset.text = "Delete Transactions"
            binding.btnReset.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }
}
