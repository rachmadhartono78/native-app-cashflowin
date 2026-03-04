package com.example.cashflowin.ui.asset

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.databinding.ActivityTransferAssetBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import com.example.cashflowin.utils.TokenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransferAssetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferAssetBinding
    private val viewModel: TransferAssetViewModel by viewModels {
        val apiService = ApiClient.getApiService(this)
        val repository = AssetRepository(apiService)
        AssetViewModelFactory(repository)
    }
    private lateinit var tokenManager: TokenManager

    private var availableAssets: List<AssetInfo> = emptyList()
    private var selectedSourceId: Int = -1
    private var selectedDestId: Int = -1

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferAssetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        // Set default date to today
        updateDateInput()

        setupListeners()
        setupObservers()

        viewModel.loadAssets()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.etTransferAmount.addTextChangedListener(CurrencyTextWatcher(binding.etTransferAmount))
        
        binding.etTransferDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateInput()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.spinnerSourceAsset.setOnItemClickListener { _, _, position, _ ->
            selectedSourceId = availableAssets[position].id
        }

        binding.spinnerDestAsset.setOnItemClickListener { _, _, position, _ ->
            selectedDestId = availableAssets[position].id
        }

        binding.btnSubmitTransfer.setOnClickListener {
            val amountFormatted = binding.etTransferAmount.text.toString().trim()
            val amount = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toString()
            val date = binding.etTransferDate.text.toString().trim()
            val description = binding.etTransferDescription.text.toString().trim()

            if (selectedSourceId == -1 || selectedDestId == -1) {
                Toast.makeText(this, "Please select source and destination assets", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (selectedSourceId == selectedDestId) {
                Toast.makeText(this, "Source and destination cannot be the same", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amountFormatted.isEmpty() || amount == "0.0") {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.submitTransfer(selectedSourceId, selectedDestId, amount, date, description)
        }
    }
    
    private fun updateDateInput() {
        val format = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale.US)
        binding.etTransferDate.setText(sdf.format(calendar.time))
    }

    private fun setupObservers() {
        viewModel.transferState.observe(this) { state ->
            when (state) {
                is TransferAssetState.Idle -> {}
                is TransferAssetState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmitTransfer.isEnabled = false
                }
                is TransferAssetState.AssetsLoaded -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmitTransfer.isEnabled = true
                    availableAssets = state.assets
                    
                    val assetNames = availableAssets.map { it.name }
                    val adapterSource = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, assetNames)
                    val adapterDest = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, assetNames)
                    
                    binding.spinnerSourceAsset.setAdapter(adapterSource)
                    binding.spinnerDestAsset.setAdapter(adapterDest)
                }
                is TransferAssetState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Asset transfer completed successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is TransferAssetState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmitTransfer.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
