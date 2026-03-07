package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.databinding.ActivityDebtSimulationBinding
import java.text.NumberFormat
import java.util.*
import kotlin.math.pow

class DebtSimulationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtSimulationBinding
    private var currentAmount = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtSimulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        setupAmountFormatting()
    }

    private fun setupAmountFormatting() {
        binding.etLoanAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != currentAmount) {
                    binding.etLoanAmount.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp,. ]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(parsed)

                        currentAmount = formatted
                        binding.etLoanAmount.setText(formatted)
                        binding.etLoanAmount.setSelection(formatted.length)
                    } else {
                        currentAmount = ""
                        binding.etLoanAmount.setText("")
                    }

                    binding.etLoanAmount.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnCalculate.setOnClickListener {
            calculateDebt()
        }

        binding.rgInterestMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbFlat.id) {
                binding.tvMethodNote.text = "*Simulasi ini menggunakan perhitungan bunga tetap (Flat Rate). Hasil akhir mungkin berbeda tergantung kebijakan penyedia pinjaman."
            } else {
                binding.tvMethodNote.text = "*Simulasi ini menggunakan perhitungan bunga Anuitas (Efektif). Cicilan bulanan tetap, namun komposisi pokok dan bunga berubah tiap bulan."
            }
        }
    }

    private fun calculateDebt() {
        val loanAmountStr = binding.etLoanAmount.text.toString().replace("[.,]".toRegex(), "")
        val interestRateStr = binding.etInterestRate.text.toString()
        val loanTermStr = binding.etLoanTerm.text.toString()

        if (loanAmountStr.isEmpty() || interestRateStr.isEmpty() || loanTermStr.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val loanAmount = loanAmountStr.toDouble()
        val annualInterestRate = interestRateStr.toDouble()
        val loanTermMonths = loanTermStr.toInt()

        var monthlyPayment = 0.0
        var totalInterest = 0.0
        var totalPayment = 0.0

        if (binding.rbFlat.isChecked) {
            totalInterest = loanAmount * (annualInterestRate / 100) * (loanTermMonths.toDouble() / 12)
            totalPayment = loanAmount + totalInterest
            monthlyPayment = totalPayment / loanTermMonths
        } else {
            val monthlyRate = (annualInterestRate / 100) / 12
            if (monthlyRate > 0) {
                monthlyPayment = (loanAmount * monthlyRate) / (1 - (1 + monthlyRate).pow(-loanTermMonths.toDouble()))
                totalPayment = monthlyPayment * loanTermMonths
                totalInterest = totalPayment - loanAmount
            } else {
                monthlyPayment = loanAmount / loanTermMonths
                totalPayment = loanAmount
                totalInterest = 0.0
            }
        }

        displayResult(monthlyPayment, totalInterest, totalPayment)
    }

    private fun displayResult(monthly: Double, interest: Double, total: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }

        binding.tvMonthlyPayment.text = format.format(monthly)
        binding.tvTotalInterest.text = format.format(interest)
        binding.tvTotalPayment.text = format.format(total)

        binding.cardResult.visibility = View.VISIBLE
    }
}
