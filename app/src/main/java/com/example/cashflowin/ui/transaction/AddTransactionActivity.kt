package com.example.cashflowin.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cashflowin.R
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.api.model.Debt
import com.example.cashflowin.api.model.Goal
import com.example.cashflowin.api.model.TransactionRequest
import com.example.cashflowin.databinding.ActivityAddTransactionBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import com.example.cashflowin.utils.TokenManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private val viewModel: AddTransactionViewModel by viewModels {
        val apiService = ApiClient.getApiService(this)
        val repository = TransactionRepository(apiService)
        TransactionViewModelFactory(application, repository)
    }
    private lateinit var tokenManager: TokenManager

    private var categoriesList: List<CategoryInfo> = emptyList()
    private var assetsList: List<AssetInfo> = emptyList()
    private var debtsList: List<Debt> = emptyList()
    private var goalsList: List<Goal> = emptyList()

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
        setupDateTimePicker()
        setupObservers()
        setupListeners()
        checkEditMode()
        updateTypeUi() // Set initial color based on selection

        if (!isEditMode) {
            calendar.time = java.util.Date()
            updateDateTimeInView()
        }

        viewModel.loadDropdownData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupDateTimePicker() {
        binding.etDate.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    
                    TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            updateDateTimeInView()
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun updateDateTimeInView() {
        val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        binding.etDate.setText(displayFormat.format(calendar.time))
    }

    private fun checkEditMode() {
        editTransactionId = intent.getIntExtra("EXTRA_ID", -1)
        if (editTransactionId != -1) {
            isEditMode = true
            supportActionBar?.title = getString(R.string.title_edit_transaction)
            binding.btnSave.text = getString(R.string.btn_update_transaction)

            initialAmount = intent.getStringExtra("EXTRA_AMOUNT")
            initialType = intent.getStringExtra("EXTRA_TYPE")
            initialDesc = intent.getStringExtra("EXTRA_DESC")
            initialDate = intent.getStringExtra("EXTRA_DATE")
            initialCategoryId = intent.getIntExtra("EXTRA_CATEGORY_ID", -1)
            initialAssetId = intent.getIntExtra("EXTRA_ASSET_ID", -1)

            if (initialAmount != null) {
                try {
                    val amountLong = initialAmount!!.toDouble().toLong()
                    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
                    symbols.groupingSeparator = '.'
                    val formatter = DecimalFormat("#,###", symbols)
                    binding.etAmount.setText(formatter.format(amountLong))
                } catch (e: Exception) {
                    binding.etAmount.setText(initialAmount)
                }
            }

            binding.etDescription.setText(initialDesc)
            
            if (initialDate != null) {
                try {
                    val inputFormats = arrayOf(
                        "yyyy-MM-dd HH:mm:ss", 
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", 
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd"
                    )
                    
                    var parsedDate: java.util.Date? = null
                    for (format in inputFormats) {
                        try {
                            val sdf = SimpleDateFormat(format, Locale.US)
                            if (format.contains("'Z'")) sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            parsedDate = sdf.parse(initialDate!!)
                            if (parsedDate != null) break
                        } catch (e: Exception) {}
                    }
                    
                    if (parsedDate != null) {
                        calendar.time = parsedDate
                        updateDateTimeInView()
                    }
                } catch (e: Exception) {}
            }

            if (initialType == "income") {
                binding.rbIncome.isChecked = true
            } else {
                binding.rbExpense.isChecked = true
            }
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
        viewModel.categoriesState.observe(this) { state ->
            if (state is DropdownState.Success) {
                categoriesList = state.data
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoriesList.map { it.name })
                binding.spinnerCategory.adapter = adapter
                populateSpinnersIfEditing()
            }
        }

        viewModel.assetsState.observe(this) { state ->
            if (state is DropdownState.Success) {
                assetsList = state.data
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, assetsList.map { it.name })
                binding.spinnerAsset.adapter = adapter
                binding.spinnerDestAsset.adapter = adapter // Reuse same list for destination
                populateSpinnersIfEditing()
            }
        }

        viewModel.debtsState.observe(this) { state ->
            if (state is DropdownState.Success) {
                debtsList = state.data
                val names = mutableListOf(getString(R.string.label_no_relation))
                names.addAll(debtsList.map { "${it.person_name} (${it.type})" })
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
                binding.spinnerDebt.adapter = adapter
            }
        }

        viewModel.goalsState.observe(this) { state ->
            if (state is DropdownState.Success) {
                goalsList = state.data
                val names = mutableListOf(getString(R.string.label_no_relation))
                names.addAll(goalsList.map { it.name })
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
                binding.spinnerGoal.adapter = adapter
            }
        }

        viewModel.submitState.observe(this) { state ->
            when (state) {
                is SubmitState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is SubmitState.Success, is SubmitState.UpdateSuccess, is SubmitState.DeleteSuccess -> {
                    Toast.makeText(this, getString(R.string.message_save_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            }
        }
    }

    private fun setupListeners() {
        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))
        
        binding.rgType.setOnCheckedChangeListener { _, _ ->
            updateTypeUi()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateTypeUi() {
        val isTransfer = binding.rbTransfer.isChecked
        
        binding.layoutDestAsset.visibility = if (isTransfer) View.VISIBLE else View.GONE
        binding.spinnerCategory.isEnabled = !isTransfer // Categories usually not needed for internal transfers
        
        val color = when {
            binding.rbIncome.isChecked -> ContextCompat.getColor(this, R.color.income)
            binding.rbTransfer.isChecked -> ContextCompat.getColor(this, R.color.text_main)
            else -> ContextCompat.getColor(this, R.color.expense)
        }
        binding.etAmount.setTextColor(color)
    }

    private fun saveTransaction() {
        val amountFormatted = binding.etAmount.text.toString().trim()
        val amountStr = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toString()
        val descStr = binding.etDescription.text.toString().trim()
        
        val isTransfer = binding.rbTransfer.isChecked
        val isAdjustment = binding.cbIsAdjustment.isChecked
        
        val type = when {
            binding.rbIncome.isChecked -> "income"
            isTransfer -> "expense" // Transfer outgoing
            else -> "expense"
        }
        
        // Use API format for date string
        val apiFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val dateStr = apiFormat.format(calendar.time)
        
        val selectedCategoryPos = binding.spinnerCategory.selectedItemPosition
        val selectedAssetPos = binding.spinnerAsset.selectedItemPosition
        val selectedDestAssetPos = binding.spinnerDestAsset.selectedItemPosition
        val selectedDebtPos = binding.spinnerDebt.selectedItemPosition
        val selectedGoalPos = binding.spinnerGoal.selectedItemPosition

        if (amountFormatted.isEmpty() || amountStr == "0") {
            binding.etAmount.error = getString(R.string.error_enter_amount)
            return
        }
        if (!isTransfer && selectedCategoryPos == -1) {
            Toast.makeText(this, getString(R.string.error_select_category), Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedAssetPos == -1) {
            Toast.makeText(this, getString(R.string.error_select_asset), Toast.LENGTH_SHORT).show()
            return
        }

        if (isTransfer) {
            if (selectedDestAssetPos == -1) {
                Toast.makeText(this, getString(R.string.error_select_dest_asset), Toast.LENGTH_SHORT).show()
                return
            }
            if (selectedAssetPos == selectedDestAssetPos) {
                Toast.makeText(this, getString(R.string.error_same_asset), Toast.LENGTH_SHORT).show()
                return
            }

            val transferRequest = com.example.cashflowin.api.model.TransferAssetRequest(
                source_asset_id = assetsList[selectedAssetPos].id,
                destination_asset_id = assetsList[selectedDestAssetPos].id,
                amount = amountStr,
                date = dateStr,
                description = descStr.ifEmpty { null }
            )
            viewModel.submitTransfer(transferRequest)
            return
        }

        val request = TransactionRequest(
            amount = amountStr,
            type = type,
            category_id = categoriesList[selectedCategoryPos].id,
            asset_id = assetsList[selectedAssetPos].id,
            description = descStr,
            date = dateStr,
            is_transfer = false,
            source_asset_id = null,
            destination_asset_id = null,
            debt_id = if (selectedDebtPos > 0) debtsList[selectedDebtPos - 1].id else null,
            goal_id = if (selectedGoalPos > 0) goalsList[selectedGoalPos - 1].id else null,
            is_adjustment = isAdjustment
        )

        if (isEditMode) {
            viewModel.updateTransaction(editTransactionId, request)
        } else {
            viewModel.submitTransaction(request)
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        if (isEditMode) menuInflater.inflate(R.menu.menu_add_transaction, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_delete_transaction))
                .setMessage(getString(R.string.message_delete_confirm))
                .setPositiveButton(getString(R.string.action_delete)) { _, _ -> viewModel.deleteTransaction(editTransactionId) }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
