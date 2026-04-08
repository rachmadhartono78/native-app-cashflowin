package com.example.cashflowin.ui.planning

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.api.model.RecurringTransactionRequest
import com.example.cashflowin.databinding.ActivityAddRecurringTransactionBinding
import com.example.cashflowin.api.model.ApiErrorResponse
import com.example.cashflowin.utils.CurrencyTextWatcher
import com.google.android.material.datepicker.MaterialDatePicker
import com.example.cashflowin.api.TransactionRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.widget.ArrayAdapter

class AddRecurringTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecurringTransactionBinding
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    
    private var isEdit = false
    private var transactionId = -1
    
    private var assetsList: List<AssetInfo> = emptyList()
    private var categoriesList: List<CategoryInfo> = emptyList()
    
    private var initialAssetId: Int? = null
    private var initialCategoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecurringTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        if (isEdit && transactionId != -1) {
            supportActionBar?.title = "Edit Recurring Transaction"
            binding.btnSave.text = "Simpan Perubahan"
            fetchTransactionDetails()
        }

        loadLookupData()
        setupListeners()
    }

    private fun loadLookupData() {
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddRecurringTransactionActivity)
                val repo = TransactionRepository(apiService)
                
                // Load Assets
                val assetResp = repo.getAssets()
                if (assetResp.isSuccessful && assetResp.body() != null) {
                    assetsList = assetResp.body()!!.data
                    val adapter = ArrayAdapter(this@AddRecurringTransactionActivity, android.R.layout.simple_spinner_dropdown_item, assetsList.map { it.name })
                    binding.spAsset.adapter = adapter
                    initialAssetId?.let { id ->
                        val idx = assetsList.indexOfFirst { it.id == id }
                        if (idx != -1) binding.spAsset.setSelection(idx)
                    }
                }

                // Load Categories
                val categoryResp = repo.getCategories()
                if (categoryResp.isSuccessful && categoryResp.body() != null) {
                    categoriesList = categoryResp.body()!!.data
                    val adapter = ArrayAdapter(this@AddRecurringTransactionActivity, android.R.layout.simple_spinner_dropdown_item, categoriesList.map { it.name })
                    binding.spCategory.adapter = adapter
                    initialCategoryId?.let { id ->
                        val idx = categoriesList.indexOfFirst { it.id == id }
                        if (idx != -1) binding.spCategory.setSelection(idx)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Gagal meload referensi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchTransactionDetails() {
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddRecurringTransactionActivity)
                val response = apiService.getRecurringTransactionDetails(transactionId)
                if (response.isSuccessful && response.body() != null) {
                    val transaction = response.body()!!.data
                    populateData(transaction)
                } else {
                    Toast.makeText(this@AddRecurringTransactionActivity, "Gagal meload data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Kesalahan jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateData(transaction: com.example.cashflowin.api.model.RecurringTransaction) {
        val descParts = transaction.description?.split(" - ", limit = 2)
        val name = descParts?.getOrNull(0) ?: ""
        val desc = descParts?.getOrNull(1) ?: ""

        binding.etName.setText(name)
        binding.etDescription.setText(desc)
        binding.etAmount.setText(transaction.amount.toLong().toString()) // TextWatcher will format this
        
        // Select Type
        for (i in 0 until binding.spType.count) {
            if (binding.spType.getItemAtPosition(i).toString().equals(transaction.type, ignoreCase = true)) {
                binding.spType.setSelection(i)
                break
            }
        }

        // Select Frequency
        for (i in 0 until binding.spFrequency.count) {
            if (binding.spFrequency.getItemAtPosition(i).toString().equals(transaction.frequency, ignoreCase = true)) {
                binding.spFrequency.setSelection(i)
                break
            }
        }

        binding.cbAutoExecute.isChecked = transaction.auto_execute
        
        initialAssetId = transaction.asset_id
        initialCategoryId = transaction.category_id
        
        // Try selecting in spinners if they are already loaded
        if (assetsList.isNotEmpty()) {
            val idx = assetsList.indexOfFirst { it.id == transaction.asset_id }
            if (idx != -1) binding.spAsset.setSelection(idx)
        }
        if (categoriesList.isNotEmpty()) {
            val idx = categoriesList.indexOfFirst { it.id == transaction.category_id }
            if (idx != -1) binding.spCategory.setSelection(idx)
        }

        try {
            val sDateParsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(transaction.start_date.replace("T"," ").replace("Z",""))
            if(sDateParsed != null) {
                binding.etStartDate.setText(displayDateFormat.format(sDateParsed))
                binding.etStartDate.tag = apiDateFormat.format(sDateParsed)
            }
        } catch (e: Exception) {
            binding.etStartDate.setText(transaction.start_date)
            binding.etStartDate.tag = transaction.start_date
        }

        if (transaction.end_date != null) {
            try {
                val eDateParsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(transaction.end_date.replace("T"," ").replace("Z",""))
                if(eDateParsed != null) {
                   binding.etEndDate.setText(displayDateFormat.format(eDateParsed))
                   binding.etEndDate.tag = apiDateFormat.format(eDateParsed)
                }
            } catch (e: Exception) {
                binding.etEndDate.setText(transaction.end_date)
                binding.etEndDate.tag = transaction.end_date
            }
        }
    }

    private fun setupListeners() {
        // Parsing Angka Otomatis (Rp 1.000.000)
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))

        binding.etStartDate.setOnClickListener {
            showDatePicker { date ->
                binding.etStartDate.setText(displayDateFormat.format(date))
                binding.etStartDate.tag = apiDateFormat.format(date)
            }
        }

        binding.etEndDate.setOnClickListener {
            showDatePicker { date ->
                binding.etEndDate.setText(displayDateFormat.format(date))
                binding.etEndDate.tag = apiDateFormat.format(date)
            }
        }

        binding.btnSave.setOnClickListener {
            saveRecurringTransaction()
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            onDateSelected(calendar.time)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun saveRecurringTransaction() {
        val nameInput = binding.etName.text.toString().trim()
        val descInput = binding.etDescription.text.toString().trim()
        
        // Gabungkan name dan description karena backend hanya punya kolom description
        val finalDescription = if (descInput.isNotEmpty()) {
            "$nameInput - $descInput"
        } else {
            nameInput
        }
        
        // Ambil nilai asli tanpa titik pemisah ribuan
        val amountFormatted = binding.etAmount.text.toString().trim()
        val amountStr = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toString()
        
        val type = binding.spType.selectedItem.toString().lowercase()
        val frequency = binding.spFrequency.selectedItem.toString().lowercase()
        val startDate = binding.etStartDate.tag?.toString() ?: ""
        val endDate = binding.etEndDate.tag?.toString()

        if (nameInput.isEmpty()) {
            binding.etName.error = "Nama wajib diisi"
            return
        }
        if (amountFormatted.isEmpty() || amountStr == "0") {
            binding.etAmount.error = "Jumlah wajib diisi"
            return
        }
        if (startDate.isEmpty()) {
            Toast.makeText(this, "Pilih tanggal mulai", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAssetPos = binding.spAsset.selectedItemPosition
        val selectedCategoryPos = binding.spCategory.selectedItemPosition
        
        if (selectedAssetPos == -1 || selectedCategoryPos == -1) {
            Toast.makeText(this, "Pilih dompet dan kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RecurringTransactionRequest(
            description = finalDescription,
            amount = amountStr.toDoubleOrNull() ?: 0.0,
            category_id = categoriesList[selectedCategoryPos].id,
            asset_id = assetsList[selectedAssetPos].id,
            type = type,
            frequency = frequency,
            frequency_interval = 1,
            start_date = startDate,
            end_date = endDate?.ifEmpty { null },
            auto_execute = binding.cbAutoExecute.isChecked
        )

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@AddRecurringTransactionActivity)
                val response = if (isEdit) {
                    apiService.updateRecurringTransaction(transactionId, request)
                } else {
                    apiService.addRecurringTransaction(request)
                }

                if (response.isSuccessful) {
                    val msg = if (isEdit) "Berhasil diperbarui" else "Berhasil ditambahkan"
                    Toast.makeText(this@AddRecurringTransactionActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = parseError(response)
                    Toast.makeText(this@AddRecurringTransactionActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddRecurringTransactionActivity, "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseError(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val errorResponse = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                errorResponse.message ?: "Error: ${response.code()}"
            } else {
                "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            "Gagal memproses error: ${response.code()}"
        }
    }
}
