package com.example.cashflowin.ui.planning

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
            startActivity(android.content.Intent(this, EmergencyFundActivity::class.java))
        }
        
        binding.cardRetirement.setOnClickListener {
            startActivity(android.content.Intent(this, RetirementFundActivity::class.java))
        }
    }
}
