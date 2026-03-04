package com.example.cashflowin.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.api.model.TransactionRequest
import com.example.cashflowin.databinding.ActivityAddTransactionBinding
import com.example.cashflowin.utils.TokenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private val viewModel: AddTransactionViewModel by viewModels {
        val apiService = ApiClient.getApiService(this)
        val repository = TransactionRepository(apiService)
        TransactionViewModelFactory(repository)
    }
    private lateinit var tokenManager: TokenManager

    private var categoriesList: List<CategoryInfo> = emptyList()
    private var assetsList: List<AssetInfo> = emptyList()

    private val calendar = Calendar.getInstance()

    // Edit Mode properties
    private var isEditMode = false
    private var editTransactionId: Int = -1
    private var initialAmount: String? = null
    private var initialType: String? = null
    private var initialDesc: String? = null
    private var initialDate: String? = null
    private var initialCategoryId: Int = -1
    private var initialAssetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupToolbar()
        setupDatePicker()
        setupObservers()
        setupListeners()
        checkEditMode()

        if (!isEditMode) {
            updateDateInView()
        }

        viewModel.loadDropdownData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etDate.setText(sdf.format(calendar.time))
    }

    private fun checkEditMode() {
        editTransactionId = intent.getIntExtra("EXTRA_ID", -1)
        if (editTransactionId != -1) {
            isEditMode = true
            supportActionBar?.title = "Edit Transaction"
            binding.btnSave.text = "Update Transaction"

            initialAmount = intent.getStringExtra("EXTRA_AMOUNT")
            initialType = intent.getStringExtra("EXTRA_TYPE")
            initialDesc = intent.getStringExtra("EXTRA_DESC")
            initialDate = intent.getStringExtra("EXTRA_DATE")
            initialCategoryId = intent.getIntExtra("EXTRA_CATEGORY_ID", -1)
            initialAssetId = intent.getIntExtra("EXTRA_ASSET_ID", -1)

            binding.etAmount.setText(initialAmount)
            binding.etDescription.setText(initialDesc)
            if (initialDate != null) {
                binding.etDate.setText(initialDate)
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    sdf.parse(initialDate!!)?.let { calendar.time = it }
                } catch (e: Exception) {}
            }

            if (initialType == "income") {
                binding.rbIncome.isChecked = true
            } else if (initialType == "expense") {
                binding.rbExpense.isChecked = true
            }
        } else {
            supportActionBar?.title = "Add Transaction"
        }
    }

    private fun populateSpinnersIfEditing() {
        if (!isEditMode) return

        if (categoriesList.isNotEmpty() && initialCategoryId != -1) {
            val index = categoriesList.indexOfFirst { it.id == initialCategoryId }
            if (index != -1) binding.spinnerCategory.setSelection(index)
        }

        if (assetsList.isNotEmpty() && initialAssetId != -1) {
            val index = assetsList.indexOfFirst { it.id == initialAssetId }
            if (index != -1) binding.spinnerAsset.setSelection(index)
        }
    }

    private fun setupObservers() {
        // Categories
        viewModel.categoriesState.observe(this) { state ->
            when (state) {
                is DropdownState.Loading -> { }
                is DropdownState.Success -> {
                    categoriesList = state.data
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categoriesList.map { it.name }
                    )
                    binding.spinnerCategory.adapter = adapter
                    populateSpinnersIfEditing()
                }
                is DropdownState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Assets
        viewModel.assetsState.observe(this) { state ->
            when (state) {
                is DropdownState.Loading -> { }
                is DropdownState.Success -> {
                    assetsList = state.data
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        assetsList.map { it.name }
                    )
                    binding.spinnerAsset.adapter = adapter
                    populateSpinnersIfEditing()
                }
                is DropdownState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Submit
        viewModel.submitState.observe(this) { state ->
            when (state) {
                is SubmitState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
                is SubmitState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is SubmitState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SubmitState.UpdateSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SubmitState.DeleteSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, "Transaction deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString().trim()
        val dateStr = binding.etDate.text.toString().trim()
        val descStr = binding.etDescription.text.toString().trim()
        
        val type = if (binding.rbIncome.isChecked) "income" else "expense"
        
        val selectedCategoryPos = binding.spinnerCategory.selectedItemPosition
        val selectedAssetPos = binding.spinnerAsset.selectedItemPosition

        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            return
        }
        if (selectedCategoryPos == -1 || categoriesList.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedAssetPos == -1 || assetsList.isEmpty()) {
            Toast.makeText(this, "Please select an asset", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TransactionRequest(
            amount = amountStr,
            type = type,
            category_id = categoriesList[selectedCategoryPos].id,
            asset_id = assetsList[selectedAssetPos].id,
            description = descStr,
            date = dateStr
        )

        if (isEditMode) {
            viewModel.updateTransaction(editTransactionId, request)
        } else {
            viewModel.submitTransaction(request)
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        if (isEditMode) {
            menuInflater.inflate(com.example.cashflowin.R.menu.menu_add_transaction, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == com.example.cashflowin.R.id.action_delete) {
            if (editTransactionId != -1) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteTransaction(editTransactionId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
