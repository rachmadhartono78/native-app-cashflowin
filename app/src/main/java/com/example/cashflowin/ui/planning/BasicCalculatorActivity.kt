package com.example.cashflowin.ui.planning

import android.os.Bundle
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.databinding.ActivityBasicCalculatorBinding
import java.text.DecimalFormat

class BasicCalculatorActivity : BaseActivity() {

    private lateinit var binding: ActivityBasicCalculatorBinding
    private var expression = ""
    private var lastNumeric = false
    private var stateError = false
    private var lastDot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBasicCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupButtons()
    }

    private fun setupButtons() {
        val numericButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        for (button in numericButtons) {
            button.setOnClickListener {
                if (stateError) {
                    binding.tvResult.text = button.text
                    stateError = false
                } else {
                    if (binding.tvResult.text.toString() == "0") {
                        binding.tvResult.text = button.text
                    } else {
                        binding.tvResult.append(button.text)
                    }
                }
                lastNumeric = true
            }
        }

        binding.btnDot.setOnClickListener {
            if (lastNumeric && !stateError && !lastDot) {
                binding.tvResult.append(".")
                lastNumeric = false
                lastDot = true
            }
        }

        binding.btnClear.setOnClickListener {
            binding.tvResult.text = "0"
            binding.tvExpression.text = ""
            expression = ""
            lastNumeric = false
            stateError = false
            lastDot = false
        }

        binding.btnDelete.setOnClickListener {
            val text = binding.tvResult.text.toString()
            if (text.length > 1) {
                binding.tvResult.text = text.substring(0, text.length - 1)
            } else {
                binding.tvResult.text = "0"
            }
        }

        binding.btnAdd.setOnClickListener { onOperatorClick("+") }
        binding.btnMinus.setOnClickListener { onOperatorClick("-") }
        binding.btnMultiply.setOnClickListener { onOperatorClick("×") }
        binding.btnDivide.setOnClickListener { onOperatorClick("÷") }

        binding.btnEqual.setOnClickListener {
            onEqual()
        }
    }

    private fun onOperatorClick(operator: String) {
        if (lastNumeric && !stateError) {
            expression = binding.tvResult.text.toString()
            binding.tvExpression.text = "$expression $operator"
            binding.tvResult.text = "0"
            lastNumeric = false
            lastDot = false
            this.operator = operator
            this.firstValue = expression.toDouble()
        }
    }

    private var operator = ""
    private var firstValue = 0.0

    private fun onEqual() {
        if (lastNumeric && !stateError && operator.isNotEmpty()) {
            val secondValue = binding.tvResult.text.toString().toDouble()
            var result = 0.0
            when (operator) {
                "+" -> result = firstValue + secondValue
                "-" -> result = firstValue - secondValue
                "×" -> result = firstValue * secondValue
                "÷" -> {
                    if (secondValue != 0.0) {
                        result = firstValue / secondValue
                    } else {
                        binding.tvResult.text = "Error"
                        stateError = true
                        lastNumeric = false
                        return
                    }
                }
            }
            binding.tvExpression.text = ""
            binding.tvResult.text = DecimalFormat("0.######").format(result)
            operator = ""
            lastNumeric = true
        }
    }
}