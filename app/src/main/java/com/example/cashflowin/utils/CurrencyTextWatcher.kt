package com.example.cashflowin.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Locale

class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")

            if (cleanString.isNotEmpty()) {
                try {
                    val parsed = cleanString.toDouble()
                    val formatted = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(parsed)

                    current = formatted
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                } catch (e: Exception) {
                    // handle error
                }
            } else {
                current = ""
                editText.setText("")
            }

            editText.addTextChangedListener(this)
        }
    }

    companion object {
        fun getUnformattedValue(text: String): Double {
            val cleanString = text.replace("[Rp,.\\s]".toRegex(), "")
            return cleanString.toDoubleOrNull() ?: 0.0
        }
    }
}
