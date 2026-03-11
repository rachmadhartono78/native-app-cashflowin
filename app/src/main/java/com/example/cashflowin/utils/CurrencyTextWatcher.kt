package com.example.cashflowin.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val stringText = s.toString()
        if (stringText != current) {
            editText.removeTextChangedListener(this)

            // Menghapus semua karakter non-digit
            val cleanString = stringText.replace("[^\\d]".toRegex(), "")

            if (cleanString.isNotEmpty()) {
                try {
                    val parsed = cleanString.toDouble()
                    
                    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
                    symbols.groupingSeparator = '.'
                    
                    // Format hanya angka dengan pemisah ribuan (tanpa Rp karena sudah ada di layout)
                    val formatter = DecimalFormat("#,###", symbols)
                    val formatted = formatter.format(parsed)

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
        fun getUnformattedValue(text: String): Long {
            val cleanString = text.replace("[^\\d]".toRegex(), "")
            return cleanString.toLongOrNull() ?: 0L
        }
    }
}