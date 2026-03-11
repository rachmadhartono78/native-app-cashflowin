package com.example.cashflowin.ui.planning

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.databinding.ActivityZakatCalculatorBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.NumberFormat
import java.util.*

class ZakatCalculatorActivity : BaseActivity() {

    private lateinit var binding: ActivityZakatCalculatorBinding
    private val localeID = Locale.forLanguageTag("id-ID")
    private val formatRupiah = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }
    private val GOLD_URL = "https://www.harga-emas.org/"
    private var isErrorState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZakatCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system bars (status bar & navigation bar) insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        fetchGoldPrice() 
    }

    private fun setupListeners() {
        // Tambahkan CurrencyTextWatcher agar input otomatis berformat ribuan (10.000.000)
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))
        binding.etGoldPrice.addTextChangedListener(CurrencyTextWatcher(binding.etGoldPrice))

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
            if (isErrorState) {
                openGoldPriceInBrowser()
            } else {
                fetchGoldPrice()
            }
        }

        binding.btnCalculate.setOnClickListener {
            calculateZakat()
        }

        binding.etGoldPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateNisabInfo()
        }
    }

    private fun String.cleanToDouble(): Double {
        return CurrencyTextWatcher.getUnformattedValue(this).toDouble()
    }

    private fun openGoldPriceInBrowser() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOLD_URL))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka browser", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchGoldPrice() {
        isErrorState = false
        binding.tvCheckGoldPrice.text = "Mengambil data..."
        binding.tvCheckGoldPrice.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(GOLD_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .get()

                var cleanPrice: Double? = null
                val cells = doc.select("td")
                for (i in 0 until cells.size) {
                    val text = cells[i].text()
                    if (text.contains("24 Karat", ignoreCase = true)) {
                        val nextCellText = cells.getOrNull(i + 1)?.text() ?: ""
                        val priceCandidate = nextCellText.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
                        if (priceCandidate > 500000) {
                            cleanPrice = priceCandidate
                            break
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (cleanPrice != null) {
                        binding.etGoldPrice.setText(cleanPrice.toLong().toString())
                        updateNisabInfo()
                        Toast.makeText(this@ZakatCalculatorActivity, "Harga emas diperbarui", Toast.LENGTH_SHORT).show()
                        resetButtonState()
                    } else {
                        setErrorState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setErrorState()
                }
            }
        }
    }

    private fun setErrorState() {
        isErrorState = true
        binding.tvCheckGoldPrice.isEnabled = true
        val errorText = "Gagal ambil data. "
        val actionText = "Klik di sini cek manual."
        val fullText = errorText + actionText
        val spannable = SpannableStringBuilder(fullText)
        spannable.setSpan(ForegroundColorSpan(Color.RED), 0, errorText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(Color.BLUE), errorText.length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(UnderlineSpan(), errorText.length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvCheckGoldPrice.text = spannable
    }

    private fun resetButtonState() {
        isErrorState = false
        binding.tvCheckGoldPrice.isEnabled = true
        binding.tvCheckGoldPrice.text = "Cek Harga Emas Hari Ini"
        binding.tvCheckGoldPrice.setTextColor(Color.BLUE)
    }

    private fun updateNisabInfo() {
        val goldPrice = binding.etGoldPrice.text.toString().cleanToDouble()
        val isMaal = binding.rbZakatMaal.isChecked
        val nisab = if (isMaal) goldPrice * 85 else (goldPrice * 85) / 12
        binding.tvNisabInfo.text = "Nisab: ${formatRupiah.format(nisab)}"
    }

    private fun calculateZakat() {
        val amount = binding.etAmount.text.toString().cleanToDouble()
        val goldPrice = binding.etGoldPrice.text.toString().cleanToDouble()
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
            binding.cardResult.setCardBackgroundColor(Color.parseColor("#059669"))
        } else {
            binding.tvResultValue.text = "Rp 0"
            binding.tvResultNote.text = "Harta Anda belum mencapai nisab (${formatRupiah.format(nisab)}). Anda belum wajib menunaikan zakat."
            binding.cardResult.setCardBackgroundColor(Color.parseColor("#64748B"))
        }
    }
}