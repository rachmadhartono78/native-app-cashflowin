package com.example.cashflowin.ui.planning

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.GoalRequest
import com.example.cashflowin.databinding.ActivityAddGoalBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGoalBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalBinding.inflate(layoutInflater)
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
        binding.etTargetAmount.addTextChangedListener(CurrencyTextWatcher(binding.etTargetAmount))
        binding.etInitialAmount.addTextChangedListener(CurrencyTextWatcher(binding.etInitialAmount))
        
        binding.etDeadline.setOnClickListener {
            DatePickerDialog(
                this, { _, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    binding.etDeadline.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener { saveGoal() }
    }

    private fun saveGoal() {
        val name = binding.etName.text.toString().trim()
        val targetAmountFormatted = binding.etTargetAmount.text.toString().trim()
        val targetAmountStr = CurrencyTextWatcher.getUnformattedValue(targetAmountFormatted).toString()
        val initialAmountFormatted = binding.etInitialAmount.text.toString().trim()
        val initialAmountStr = CurrencyTextWatcher.getUnformattedValue(initialAmountFormatted).toString()
        var deadlineStr: String? = binding.etDeadline.text.toString().trim()
        if (deadlineStr != null && deadlineStr.isEmpty()) {
            deadlineStr = null
        }

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }
        if (targetAmountFormatted.isEmpty()) {
            binding.etTargetAmount.error = "Target amount is required"
            return
        }

        val request = GoalRequest(
            name = name,
            target_amount = targetAmountStr.toDoubleOrNull() ?: 0.0,
            current_amount = initialAmountStr.toDoubleOrNull() ?: 0.0,
            deadline = deadlineStr
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddGoalActivity)
                val response = apiService.addGoal(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddGoalActivity, "Goal saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddGoalActivity, "Failed to save goal", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddGoalActivity, "Network error", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }
}
