package com.example.cashflowin.ui.asset

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.databinding.ActivityAddEditAssetBinding
import com.example.cashflowin.utils.CurrencyTextWatcher
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AddEditAssetActivity : BaseActivity() {

    private lateinit var binding: ActivityAddEditAssetBinding
    private val viewModel: AddEditAssetViewModel by viewModels {
        AssetViewModelFactory(AssetRepository(ApiClient.getApiService(this)))
    }

    private var isEditMode = false
    private var assetId: Int = -1
    
    // Default values
    private var selectedColor: String = "#00AA5B"
    private var selectedIcon: String = "ic_menu_gallery"

    private val assetTypes = listOf("Cash", "Bank", "E-Wallet", "Investasi", "Lainnya")
    
    private val colorPalette = listOf(
        "#00AA5B", "#3B82F6", "#8B5CF6", "#EF4444", 
        "#F59E0B", "#EC4899", "#06B6D4", "#64748B"
    )

    private val iconList = listOf(
        "ic_menu_gallery", "ic_menu_manage", "ic_menu_today",
        "ic_menu_send", "ic_menu_save", "ic_menu_agenda",
        "ic_menu_myplaces", "ic_menu_call", "ic_menu_camera"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAssetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupSpinners()
        setupListeners()
        setupObservers()
        
        checkIntentAndSetMode()
        
        // Final preview update after everything is initialized
        updatePreview()
    }

    private fun updatePreview() {
        try {
            val color = Color.parseColor(selectedColor)
            binding.cardPreview.setCardBackgroundColor(color)
            
            val resId = resources.getIdentifier(selectedIcon, "drawable", "android")
            if (resId != 0) {
                binding.ivAssetIconPreview.setImageResource(resId)
            }
            
            binding.tvAssetNamePreview.text = binding.etAssetName.text.toString().ifEmpty { "Nama Aset" }
            binding.tvAssetTypePreview.text = binding.spinnerAssetType.text.toString().ifEmpty { "Tipe Aset" }
            
            // Ambil nilai bersih (tanpa titik/formatting) lalu format ulang untuk preview
            val rawAmount = CurrencyTextWatcher.getUnformattedValue(binding.etAssetAmount.text.toString())
            val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
            symbols.groupingSeparator = '.'
            val formatter = DecimalFormat("#,###", symbols)
            val formatted = if (rawAmount > 0) formatter.format(rawAmount) else "0"
            
            binding.tvAssetAmountPreview.text = "Rp $formatted"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, assetTypes)
        binding.spinnerAssetType.setAdapter(typeAdapter)
    }

    private fun checkIntentAndSetMode() {
        assetId = intent.getIntExtra("EXTRA_ASSET_ID", -1)
        isEditMode = assetId != -1

        if (isEditMode) {
            supportActionBar?.title = "Edit Aset"
            binding.btnSaveAsset.text = "Update Aset"
            
            val name = intent.getStringExtra("EXTRA_ASSET_NAME") ?: ""
            val type = intent.getStringExtra("EXTRA_ASSET_TYPE") ?: ""
            val amountStr = intent.getStringExtra("EXTRA_ASSET_AMOUNT") ?: "0"
            selectedColor = intent.getStringExtra("EXTRA_ASSET_COLOR") ?: "#00AA5B"
            selectedIcon = intent.getStringExtra("EXTRA_ASSET_ICON") ?: "ic_menu_gallery"

            binding.etAssetName.setText(name)
            binding.spinnerAssetType.setText(type, false)
            
            // Format nominal saat edit mode dengan lebih aman
            try {
                // Bersihkan string dari karakter non-digit kecuali titik/koma desimal jika ada
                val cleanAmount = amountStr.replace(Regex("[^0-9.]"), "")
                val rawLong = cleanAmount.toDoubleOrNull()?.toLong() ?: 0L
                
                val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
                symbols.groupingSeparator = '.'
                val formatter = DecimalFormat("#,###", symbols)
                binding.etAssetAmount.setText(formatter.format(rawLong))
            } catch (e: Exception) {
                binding.etAssetAmount.setText(amountStr)
            }

            binding.etAssetAmount.isEnabled = false
            binding.layoutAmount.isEnabled = false
            binding.tvBalanceHelper.visibility = View.VISIBLE
            
            invalidateOptionsMenu()
            
            // Update preview immediately after setting data
            updatePreview()
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cardPreview.setOnClickListener { showStylePicker() }

        binding.etAssetName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updatePreview() }
        })

        binding.etAssetAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAssetAmount))
        binding.etAssetAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updatePreview() }
        })

        binding.btnSaveAsset.setOnClickListener {
            val name = binding.etAssetName.text.toString().trim()
            val type = binding.spinnerAssetType.text.toString().trim()
            val amountFormatted = binding.etAssetAmount.text.toString().trim()

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Mohon isi nama dan tipe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = CurrencyTextWatcher.getUnformattedValue(amountFormatted).toDouble()

            if (isEditMode) {
                viewModel.updateAsset(assetId, name, amount, type, selectedColor, selectedIcon)
            } else {
                viewModel.addAsset(name, amount, type, selectedColor, selectedIcon)
            }
        }
    }

    private fun showStylePicker() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_asset_style_picker, null)
        
        val rvIcons = view.findViewById<RecyclerView>(R.id.rvIcons)
        val colorPickerLayout = view.findViewById<ViewGroup>(R.id.colorPickerLayout)
        
        // Setup Icons
        rvIcons.layoutManager = GridLayoutManager(this, 5)
        rvIcons.adapter = IconAdapter(iconList) { iconName ->
            selectedIcon = iconName
            updatePreview()
        }
        
        // Setup Colors
        colorPalette.forEach { colorStr ->
            val colorView = View(this).apply {
                val size = (36 * resources.displayMetrics.density).toInt()
                layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                    rightMargin = (16 * resources.displayMetrics.density).toInt()
                }
                background = ContextCompat.getDrawable(context, R.drawable.circle_allocation_green)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorStr))
                setOnClickListener {
                    selectedColor = colorStr
                    updatePreview()
                }
            }
            colorPickerLayout.addView(colorView)
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun setupObservers() {
        viewModel.submitState.observe(this) { state ->
            when (state) {
                is AssetSubmitState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSaveAsset.isEnabled = false
                }
                is AssetSubmitState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    finish()
                }
                is AssetSubmitState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveAsset.isEnabled = true
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
            .setTitle("Hapus Aset")
            .setMessage("Yakin ingin menghapus aset ini?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteAsset(assetId) }
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