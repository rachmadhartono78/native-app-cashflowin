package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
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

        binding.cardEmergencyFund.setOnClickListener {
            // Navigate to Emergency Fund Calculator Activity if exists
            // startActivity(Intent(this, EmergencyFundActivity::class.java))
        }

        binding.cardRetirement.setOnClickListener {
            // Navigate to Retirement Fund Calculator Activity if exists
            // startActivity(Intent(this, RetirementFundActivity::class.java))
        }
    }
}
