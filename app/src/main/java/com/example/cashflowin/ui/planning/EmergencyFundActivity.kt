package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.databinding.ActivityEmergencyFundBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import java.text.NumberFormat
import java.util.Locale

class EmergencyFundActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyFundBinding
    private val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyFundBinding.inflate(layoutInflater)
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
            val expenseStr = CurrencyTextWatcher.getUnformattedValue(binding.etMonthlyExpense.text.toString()).toString()
            val expense = expenseStr.toDoubleOrNull() ?: 0.0

            if (expense <= 0) {
                binding.etMonthlyExpense.error = "Masukkan pengeluaran bulanan valid"
                return@setOnClickListener
            }

            val multiplier = when {
                binding.rbSingle.isChecked -> 6
                binding.rbMarriedNoKids.isChecked -> 9
                else -> 12
            }

            val idealFund = expense * multiplier

            binding.tvResult.text = format.format(idealFund)
            binding.cardResult.visibility = View.VISIBLE
        }
    }
}
