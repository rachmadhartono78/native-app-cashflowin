package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.databinding.ActivityCalculatorBinding

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
    }

    private fun setupListeners() {
        binding.cardEmergencyFund.setOnClickListener {
            try {
                val intent = Intent(this, EmergencyFundActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                showUnderDevelopmentMessage()
            }
        }

        binding.cardRetirement.setOnClickListener {
            try {
                val intent = Intent(this, RetirementFundActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                showUnderDevelopmentMessage()
            }
        }

        binding.cardDebt.setOnClickListener {
            try {
                val intent = Intent(this, DebtSimulationActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                showUnderDevelopmentMessage()
            }
        }

        binding.cardBasic.setOnClickListener {
            try {
                val intent = Intent(this, BasicCalculatorActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                showUnderDevelopmentMessage()
            }
        }
    }

    private fun showUnderDevelopmentMessage() {
        Toast.makeText(this, "Maaf, fitur ini sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
    }
}
