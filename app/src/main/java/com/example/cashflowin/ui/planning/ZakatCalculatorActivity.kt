package com.example.cashflowin.ui.planning

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.databinding.ActivityZakatCalculatorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.NumberFormat
import java.util.*

class ZakatCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityZakatCalculatorBinding
    private val localeID = Locale.forLanguageTag("id-ID")
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
        fetchGoldPrice() // Ambil harga emas saat app dibuka
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
            fetchGoldPrice() // Refresh harga emas
        }

        binding.btnCalculate.setOnClickListener {
            calculateZakat()
        }

        binding.etGoldPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateNisabInfo()
        }
    }

    private fun fetchGoldPrice() {
        // Tampilkan indikasi loading (bisa diubah sesuai UI Anda)
        binding.tvCheckGoldPrice.text = "Mengambil data..."
        binding.tvCheckGoldPrice.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Mencoba mengambil harga emas dari harga-emas.org
                // Note: Struktur web bisa berubah, ini adalah contoh scraping dasar
                val doc = Jsoup.connect("https://www.harga-emas.org/").get()
                // Mencari elemen harga emas per gram (biasanya ada di tabel pertama)
                val priceText = doc.select("table.table-price tr:contains(24 Karat) td").get(1).text()
                
                // Membersihkan string (Contoh: "Rp 1.200.000" -> 1200000)
                val cleanPrice = priceText.replace(Regex("[^0-9]"), "").toDoubleOrNull()

                withContext(Dispatchers.Main) {
                    if (cleanPrice != null) {
                        binding.etGoldPrice.setText(cleanPrice.toLong().toString())
                        updateNisabInfo()
                        Toast.makeText(this@ZakatCalculatorActivity, "Harga emas diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    resetButtonState()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ZakatCalculatorActivity, "Gagal mengambil harga emas terbaru", Toast.LENGTH_SHORT).show()
                    resetButtonState()
                }
            }
        }
    }

    private fun resetButtonState() {
        binding.tvCheckGoldPrice.text = "Cek Harga Emas Hari Ini"
        binding.tvCheckGoldPrice.isEnabled = true
    }

    private fun updateNisabInfo() {
        val goldPrice = binding.etGoldPrice.text.toString().toDoubleOrNull() ?: 0.0
        val isMaal = binding.rbZakatMaal.isChecked
        
        val nisab = if (isMaal) {
            goldPrice * 85 
        } else {
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
        val zakatRate = 0.025 

        binding.cardResult.visibility = View.VISIBLE
        
        if (amount >= nisab) {
            val zakatWajib = amount * zakatRate
            binding.tvResultValue.text = formatRupiah.format(zakatWajib)
            binding.tvResultNote.text = "Harta Anda sudah mencapai nisab. Anda wajib menunaikan zakat."
            binding.cardResult.setCardBackgroundColor(android.graphics.Color.parseColor("#059669"))
        } else {
            binding.tvResultValue.text = "Rp 0"
            binding.tvResultNote.text = "Harta Anda belum mencapai nisab (${formatRupiah.format(nisab)}). Anda belum wajib menunaikan zakat."
            binding.cardResult.setCardBackgroundColor(android.graphics.Color.parseColor("#64748B"))
        }
    }
}
