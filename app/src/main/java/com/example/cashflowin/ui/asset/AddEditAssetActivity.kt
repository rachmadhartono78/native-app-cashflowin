package com.example.cashflowin.ui.asset

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.R
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.databinding.ActivityAddEditAssetBinding
import com.example.cashflowin.utils.CurrencyTextWatcher

class AddEditAssetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditAssetBinding
    private val viewModel: AddEditAssetViewModel by viewModels {
        AssetViewModelFactory(AssetRepository(ApiClient.getApiService(this)))
    }

    private var isEditMode = false
    private var assetId: Int = -1

    private val assetTypes = listOf("Cash", "Bank", "E-Wallet", "Investasi", "Lainnya")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAssetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupSpinners()
        checkIntentAndSetMode()
        setupListeners()
        setupObservers()
    }

    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, assetTypes)
        binding.spinnerAssetType.setAdapter(typeAdapter)
    }

    private fun checkIntentAndSetMode() {
        assetId = intent.getIntExtra("EXTRA_ASSET_ID", -1)
        isEditMode = assetId != -1

        if (isEditMode) {
            supportActionBar?.title = "Edit Asset"
            binding.btnSaveAsset.text = "Update Asset"
            
            val name = intent.getStringExtra("EXTRA_ASSET_NAME") ?: ""
            val type = intent.getStringExtra("EXTRA_ASSET_TYPE") ?: ""
            val amount = intent.getStringExtra("EXTRA_ASSET_AMOUNT") ?: "0"

            binding.etAssetName.setText(name)
            binding.spinnerAssetType.setText(type, false)
            binding.etAssetAmount.setText(amount)

            binding.etAssetAmount.isEnabled = false
            binding.layoutAmount.isEnabled = false
            binding.tvBalanceHelper.visibility = View.VISIBLE
            
            invalidateOptionsMenu()
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.etAssetAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAssetAmount))

        binding.btnSaveAsset.setOnClickListener {
            val name = binding.etAssetName.text.toString().trim()
            val type = binding.spinnerAssetType.text.toString().trim()
            val amountFormatted = binding.etAssetAmount.text.toString().trim()

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Please fill name and type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = CurrencyTextWatcher.getUnformattedValue(amountFormatted)

            if (isEditMode) {
                viewModel.updateAsset(assetId, name, amount, type)
            } else {
                viewModel.addAsset(name, amount, type)
            }
        }
    }

    private fun setupObservers() {
        viewModel.submitState.observe(this) { state ->
            when (state) {
                is AssetSubmitState.Idle -> {}
                is AssetSubmitState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSaveAsset.isEnabled = false
                }
                is AssetSubmitState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val msg = if (isEditMode) "Asset updated successfully!" else "Asset created successfully!"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is AssetSubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveAsset.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEditMode) {
            menuInflater.inflate(R.menu.menu_add_transaction, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            showDeleteConfirmation()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Asset")
            .setMessage("Are you sure you want to delete this asset? (This will also delete associated transactions).")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAsset(assetId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
