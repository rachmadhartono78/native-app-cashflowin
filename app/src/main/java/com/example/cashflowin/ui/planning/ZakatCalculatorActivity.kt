package com.example.cashflowin.ui.planning

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.databinding.ActivityZakatCalculatorBinding
import java.text.NumberFormat
import java.util.*

class ZakatCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityZakatCalculatorBinding
    private val localeID = Locale("in", "ID")
    private val formatRupiah = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZakatCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        updateNisabInfo()
    }

    private fun setupListeners() {
        binding.rgZakatType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbZakatMaal.id) {
                binding.tvInputTitle.text = "Total Harta (Tabungan/Emas/Lainnya)"
            } else {
                binding.tvInputTitle.text = "Pendapatan Per Bulan"
            }
            updateNisabInfo()
            binding.cardResult.visibility = View.GONE
        }

        binding.tvCheckGoldPrice.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.harga-emas.org/"))
            startActivity(intent)
        }

        binding.btnCalculate.setOnClickListener {
            calculateZakat()
        }

        // Update Nisab when gold price changes
        binding.etGoldPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateNisabInfo()
        }
    }

    private fun updateNisabInfo() {
        val goldPrice = binding.etGoldPrice.text.toString().toDoubleOrNull() ?: 0.0
        val isMaal = binding.rbZakatMaal.isChecked
        
        val nisab = if (isMaal) {
            goldPrice * 85 // Nisab Maal: 85 gram emas
        } else {
            // Nisab Profesi sering disetarakan dengan 522kg beras, 
            // namun banyak lembaga menggunakan standar 85 gram emas / 12 bulan
            (goldPrice * 85) / 12
        }
        
        binding.tvNisabInfo.text = "Nisab: ${formatRupiah.format(nisab)}"
    }

    private fun calculateZakat() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val goldPrice = binding.etGoldPrice.text.toString().toDoubleOrNull() ?: 0.0
        val isMaal = binding.rbZakatMaal.isChecked

        if (amount <= 0) {
            binding.etAmount.error = "Masukkan nominal yang valid"
            return
        }

        val nisab = if (isMaal) goldPrice * 85 else (goldPrice * 85) / 12
        val zakatRate = 0.025 // 2.5%

        binding.cardResult.visibility = View.VISIBLE
        
        if (amount >= nisab) {
            val zakatWajib = amount * zakatRate
            binding.tvResultValue.text = formatRupiah.format(zakatWajib)
            binding.tvResultNote.text = "Harta Anda sudah mencapai nisab. Anda wajib menunaikan zakat."
            binding.cardResult.setCardBackgroundColor(android.graphics.Color.parseColor("#059669")) // Success Green
        } else {
            binding.tvResultValue.text = "Rp 0"
            binding.tvResultNote.text = "Harta Anda belum mencapai nisab (${formatRupiah.format(nisab)}). Anda belum wajib menunaikan zakat maal/profesi."
            binding.cardResult.setCardBackgroundColor(android.graphics.Color.parseColor("#64748B")) // Slate Gray
        }
    }
}
