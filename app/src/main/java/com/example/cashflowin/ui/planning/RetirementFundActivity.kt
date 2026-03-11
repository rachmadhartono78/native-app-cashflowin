package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.databinding.ActivityRetirementFundBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import java.text.NumberFormat
import java.util.Locale

class RetirementFundActivity : BaseActivity() {

    private lateinit var binding: ActivityRetirementFundBinding
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRetirementFundBinding.inflate(layoutInflater)
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
        binding.etMonthlyExpense.addTextChangedListener(CurrencyTextWatcher(binding.etMonthlyExpense))

        binding.btnCalculate.setOnClickListener {
            val currentAge = binding.etCurrentAge.text.toString().toIntOrNull() ?: 0
            val retirementAge = binding.etRetirementAge.text.toString().toIntOrNull() ?: 55
            val expenseStr = CurrencyTextWatcher.getUnformattedValue(binding.etMonthlyExpense.text.toString()).toString()
            val monthlyExpense = expenseStr.toDoubleOrNull() ?: 0.0

            if (currentAge <= 0 || retirementAge <= currentAge) {
                binding.etCurrentAge.error = "Usia tidak valid"
                return@setOnClickListener
            }
            if (monthlyExpense <= 0) {
                binding.etMonthlyExpense.error = "Masukkan pengeluaran valid"
                return@setOnClickListener
            }

            // Using the 4% Rule: Annual expense * 25
            val annualExpense = monthlyExpense * 12
            val requiredFund = annualExpense * 25
            val yearsToSave = retirementAge - currentAge

            binding.tvResult.text = format.format(requiredFund)
            binding.tvYearsToSave.text = "Waktu untuk menabung: $yearsToSave Tahun"
            binding.cardResult.visibility = View.VISIBLE
        }
    }
}