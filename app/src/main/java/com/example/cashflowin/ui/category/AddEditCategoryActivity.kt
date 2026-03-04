package com.example.cashflowin.ui.category

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
import com.example.cashflowin.api.CategoryRepository
import com.example.cashflowin.databinding.ActivityAddEditCategoryBinding
import com.example.cashflowin.utils.TokenManager

class AddEditCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditCategoryBinding
    private val viewModel: AddEditCategoryViewModel by viewModels {
        val apiService = ApiClient.getApiService(this)
        val repository = CategoryRepository(apiService)
        CategoryViewModelFactory(repository)
    }
    private lateinit var tokenManager: TokenManager

    private var isEditMode = false
    private var categoryId: Int = -1

    private val categoryTypes = listOf("Pemasukan", "Pengeluaran")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupSpinners()
        checkIntentAndSetMode()
        setupListeners()
        setupObservers()
    }

    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryTypes)
        binding.spinnerCategoryType.setAdapter(typeAdapter)
    }

    private fun checkIntentAndSetMode() {
        categoryId = intent.getIntExtra("EXTRA_CATEGORY_ID", -1)
        isEditMode = categoryId != -1

        if (isEditMode) {
            supportActionBar?.title = "Edit Category"
            binding.btnSaveCategory.text = "Update Category"
            
            // Populate fields
            val name = intent.getStringExtra("EXTRA_CATEGORY_NAME") ?: ""
            val type = intent.getStringExtra("EXTRA_CATEGORY_TYPE") ?: ""

            binding.etCategoryName.setText(name)
            binding.spinnerCategoryType.setText(type, false)

            // Invalidate menu to show delete icon
            invalidateOptionsMenu()
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSaveCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            val type = binding.spinnerCategoryType.text.toString().trim()

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                viewModel.updateCategory(categoryId, name, type)
            } else {
                viewModel.saveCategory(name, type)
            }
        }
    }

    private fun setupObservers() {
        viewModel.submitState.observe(this) { state ->
            when (state) {
                is CategorySubmitState.Idle -> {}
                is CategorySubmitState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSaveCategory.isEnabled = false
                }
                is CategorySubmitState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Category created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is CategorySubmitState.UpdateSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Category updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is CategorySubmitState.DeleteSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Category deleted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is CategorySubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveCategory.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- Menu setup for Delete Icon ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEditMode) {
            // Reusing the same trash icon from phase 2
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
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete this category?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(categoryId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
