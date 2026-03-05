package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.Debt
import com.example.cashflowin.databinding.ActivityDebtDetailBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DebtDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtDetailBinding
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private var debtId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        debtId = intent.getIntExtra("EXTRA_DEBT_ID", -1)

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (debtId != -1) {
            fetchDebtDetails()
        } else {
            Toast.makeText(this, "Invalid Debt ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() {
        binding.fabAddPayment.setOnClickListener {
            val intent = android.content.Intent(this, AddPaymentActivity::class.java)
            intent.putExtra("EXTRA_DEBT_ID", debtId)
            
            // Pass the remaining amount to ensure proper validation
            val remaining = binding.tvRemainingAmount.text.toString().let {
                CurrencyTextWatcher.getUnformattedValue(it).toDoubleOrNull() ?: 0.0
            }
            intent.putExtra("EXTRA_REMAINING_AMOUNT", remaining)
            
            startActivity(intent)
        }
    }

    private fun fetchDebtDetails() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentScroll.visibility = View.GONE
        binding.fabAddPayment.hide()

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@DebtDetailActivity)
                val response = apiService.getDebtDetails(debtId)

                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val debt = response.body()!!.data
                    bindData(debt)
                    binding.contentScroll.visibility = View.VISIBLE
                    if (debt.status == "pending") {
                        binding.fabAddPayment.show()
                    }
                } else {
                    Toast.makeText(this@DebtDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@DebtDetailActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun bindData(debt: Debt) {
        binding.tvPersonName.text = debt.person_name
        
        val typeStr = if (debt.type == "debt") "Hutang Saya" else "Dihutangi"
        val statusStr = if (debt.status == "pending") "Belum Lunas" else "Lunas"
        binding.tvTypeAndStatus.text = "$typeStr • $statusStr"
        
        val remaining = debt.remaining_amount ?: debt.amount
        binding.tvRemainingAmount.text = format.format(remaining)
        binding.tvTotalAmount.text = format.format(debt.amount)
        
        if (debt.due_date != null) {
            binding.tvDueDate.visibility = View.VISIBLE
            binding.tvDueDate.text = "Jatuh Tempo: ${debt.due_date}"
        } else {
            binding.tvDueDate.visibility = View.GONE
        }
        
        if (!debt.description.isNullOrEmpty()) {
            binding.tvDescription.visibility = View.VISIBLE
            binding.tvDescription.text = debt.description
        } else {
            binding.tvDescription.visibility = View.GONE
        }
        
        if (debt.payments.isNullOrEmpty()) {
            binding.tvNoPayments.visibility = View.VISIBLE
            binding.rvPayments.visibility = View.GONE
        } else {
            binding.tvNoPayments.visibility = View.GONE
            binding.rvPayments.visibility = View.VISIBLE
            // TODO: Set up Payment Adapter
        }
    }
}
