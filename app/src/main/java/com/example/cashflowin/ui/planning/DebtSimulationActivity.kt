package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.databinding.ActivityDebtSimulationBinding
import java.text.NumberFormat
import java.util.*

class DebtSimulationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtSimulationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtSimulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCalculate.setOnClickListener {
            calculateDebt()
        }
    }

    private fun calculateDebt() {
        val loanAmountStr = binding.etLoanAmount.text.toString()
        val interestRateStr = binding.etInterestRate.text.toString()
        val loanTermStr = binding.etLoanTerm.text.toString()

        if (loanAmountStr.isEmpty() || interestRateStr.isEmpty() || loanTermStr.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val loanAmount = loanAmountStr.toDouble()
        val annualInterestRate = interestRateStr.toDouble()
        val loanTermMonths = loanTermStr.toInt()

        // Perhitungan Bunga Flat (Sesuai catatan di XML)
        val totalInterest = loanAmount * (annualInterestRate / 100) * (loanTermMonths.toDouble() / 12)
        val totalPayment = loanAmount + totalInterest
        val monthlyPayment = totalPayment / loanTermMonths

        displayResult(monthlyPayment, totalInterest, totalPayment)
    }

    private fun displayResult(monthly: Double, interest: Double, total: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        binding.tvMonthlyPayment.text = format.format(monthly)
        binding.tvTotalInterest.text = format.format(interest)
        binding.tvTotalPayment.text = format.format(total)

        binding.cardResult.visibility = View.VISIBLE
    }
}
