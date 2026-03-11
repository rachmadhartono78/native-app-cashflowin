package com.example.cashflowin.ui.category

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.BaseActivity
import com.example.cashflowin.R
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.CategoryRepository
import com.example.cashflowin.databinding.ActivityAddEditCategoryBinding
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddEditCategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityAddEditCategoryBinding
    private val viewModel: AddEditCategoryViewModel by viewModels {
        val apiService = ApiClient.getApiService(this)
        val repository = CategoryRepository(apiService)
        CategoryViewModelFactory(repository)
    }
    private lateinit var tokenManager: TokenManager

    private var isEditMode = false
    private var categoryId: Int = -1
    
    // Default values
    private var selectedColor: String = "#00AA5B" // Default Green
    private var selectedIcon: String = "ic_menu_agenda" // Default Icon name

    private val categoryTypes = listOf("Pemasukan", "Pengeluaran")
    
    // List of available colors (Modern Palette)
    private val colorPalette = listOf(
        "#00AA5B", "#3B82F6", "#8B5CF6", "#EF4444", 
        "#F59E0B", "#EC4899", "#06B6D4", "#64748B"
    )

    // List of available icons (System Resource Names)
    private val iconList = listOf(
        "ic_menu_agenda", "ic_menu_myplaces", "ic_menu_call", 
        "ic_menu_camera", "ic_menu_send", "ic_menu_gallery",
        "ic_menu_slideshow", "ic_menu_manage", "ic_menu_today"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupSpinners()
        setupColorPicker()
        checkIntentAndSetMode()
        setupListeners()
        setupObservers()
        
        updatePreview()
    }

    private fun setupColorPicker() {
        binding.colorPickerLayout.removeAllViews()
        colorPalette.forEach { colorStr ->
            val colorView = View(this).apply {
                val size = (32 * resources.displayMetrics.density).toInt()
                layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                    rightMargin = (12 * resources.displayMetrics.density).toInt()
                }
                background = ContextCompat.getDrawable(context, R.drawable.circle_allocation_green) // Base shape
                backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorStr))
                
                setOnClickListener {
                    selectedColor = colorStr
                    updatePreview()
                }
            }
            binding.colorPickerLayout.addView(colorView)
        }
    }

    private fun updatePreview() {
        try {
            binding.cardPreview.setCardBackgroundColor(Color.parseColor(selectedColor))
            binding.cardPreview.alpha = 1.0f // Ensure it's visible
            
            // Set soft background for card but solid for icon
            binding.cardPreview.backgroundTintList = ColorStateList.valueOf(Color.parseColor(selectedColor)).withAlpha(40)
            binding.ivIconPreview.imageTintList = ColorStateList.valueOf(Color.parseColor(selectedColor))
            
            // Update icon drawable
            val resId = resources.getIdentifier(selectedIcon, "drawable", "android")
            if (resId != 0) {
                binding.ivIconPreview.setImageResource(resId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showIconPickerDialog() {
        val bottomSheet = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_icon_picker, null)
        val rvIcons = dialogView.findViewById<RecyclerView>(R.id.rvIcons)
        
        rvIcons.layoutManager = GridLayoutManager(this, 4)
        rvIcons.adapter = IconAdapter(iconList) { iconName ->
            selectedIcon = iconName
            updatePreview()
            bottomSheet.dismiss()
        }
        
        bottomSheet.setContentView(dialogView)
        bottomSheet.show()
    }

    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryTypes)
        binding.spinnerCategoryType.setAdapter(typeAdapter)
    }

    private fun checkIntentAndSetMode() {
        categoryId = intent.getIntExtra("EXTRA_CATEGORY_ID", -1)
        isEditMode = categoryId != -1

        if (isEditMode) {
            supportActionBar?.title = "Edit Kategori"
            binding.btnSaveCategory.text = "Update Kategori"
            
            val name = intent.getStringExtra("EXTRA_CATEGORY_NAME") ?: ""
            val type = intent.getStringExtra("EXTRA_CATEGORY_TYPE") ?: ""
            selectedColor = intent.getStringExtra("EXTRA_CATEGORY_COLOR") ?: "#00AA5B"
            selectedIcon = intent.getStringExtra("EXTRA_CATEGORY_ICON") ?: "ic_menu_agenda"

            binding.etCategoryName.setText(name)
            binding.spinnerCategoryType.setText(type, false)
            updatePreview()
            invalidateOptionsMenu()
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cardPreview.setOnClickListener {
            showIconPickerDialog()
        }

        binding.btnSaveCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            val type = binding.spinnerCategoryType.text.toString().trim()

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                viewModel.updateCategory(categoryId, name, type, selectedColor, selectedIcon)
            } else {
                viewModel.saveCategory(name, type, selectedColor, selectedIcon)
            }
        }
    }

    private fun setupObservers() {
        viewModel.submitState.observe(this) { state ->
            when (state) {
                is CategorySubmitState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSaveCategory.isEnabled = false
                }
                is CategorySubmitState.Success, is CategorySubmitState.UpdateSuccess, is CategorySubmitState.DeleteSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    finish()
                }
                is CategorySubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveCategory.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEditMode) menuInflater.inflate(R.menu.menu_add_transaction, menu)
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
            .setTitle("Hapus Kategori")
            .setMessage("Yakin ingin menghapus kategori ini?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteCategory(categoryId) }
            .setNegativeButton("Batal", null)
            .show()
    }

    inner class IconAdapter(private val icons: List<String>, private val onIconClick: (String) -> Unit) :
        RecyclerView.Adapter<IconAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_icon_picker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iconName = icons[position]
            val resId = resources.getIdentifier(iconName, "drawable", "android")
            if (resId != 0) {
                holder.ivIcon.setImageResource(resId)
                holder.ivIcon.setOnClickListener { onIconClick(iconName) }
            }
        }

        override fun getItemCount() = icons.size
    }
}