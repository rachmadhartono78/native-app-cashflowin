package com.example.cashflowin.ui.asset

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.R
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.databinding.ActivityAssetDetailBinding
import com.example.cashflowin.ui.dashboard.TransactionAdapter
import java.text.NumberFormat
import java.util.Locale

class AssetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssetDetailBinding
    private val viewModel: AssetDetailViewModel by viewModels {
        AssetDetailViewModelFactory(AssetRepository(ApiClient.getApiService(this)))
    }

    private var assetId: Int = -1
    private var assetName: String = ""
    private var assetType: String = ""
    private var assetAmount: String = "0"
    private var assetColor: String = "#00AA5B"
    private var assetIcon: String = "ic_menu_gallery"

    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        assetId = intent.getIntExtra("EXTRA_ASSET_ID", -1)
        assetName = intent.getStringExtra("EXTRA_ASSET_NAME") ?: ""
        assetType = intent.getStringExtra("EXTRA_ASSET_TYPE") ?: ""
        assetAmount = intent.getStringExtra("EXTRA_ASSET_AMOUNT") ?: "0"
        assetColor = intent.getStringExtra("EXTRA_ASSET_COLOR") ?: "#00AA5B"
        assetIcon = intent.getStringExtra("EXTRA_ASSET_ICON") ?: "ic_menu_gallery"

        setupUI()
        setupRecyclerView()
        setupObservers()
        
        loadMutations()
    }

    private fun setupUI() {
        binding.tvAssetName.text = assetName
        binding.tvAssetType.text = assetType
        
        val amount = assetAmount.toDoubleOrNull() ?: 0.0
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        binding.tvAssetBalance.text = format.format(amount)

        try {
            val color = Color.parseColor(assetColor)
            binding.cardAsset.setCardBackgroundColor(color)
            
            val resId = resources.getIdentifier(assetIcon, "drawable", packageName)
            val androidResId = resources.getIdentifier(assetIcon, "drawable", "android")
            if (resId != 0) {
                binding.ivAssetIcon.setImageResource(resId)
            } else if (androidResId != 0) {
                binding.ivAssetIcon.setImageResource(androidResId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.btnEditAsset.setOnClickListener {
            val intent = Intent(this, AddEditAssetActivity::class.java).apply {
                putExtra("EXTRA_ASSET_ID", assetId)
                putExtra("EXTRA_ASSET_NAME", assetName)
                putExtra("EXTRA_ASSET_TYPE", assetType)
                putExtra("EXTRA_ASSET_AMOUNT", assetAmount)
                putExtra("EXTRA_ASSET_COLOR", assetColor)
                putExtra("EXTRA_ASSET_ICON", assetIcon)
            }
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Option to edit transaction if needed
        }
        binding.rvMutations.apply {
            layoutManager = LinearLayoutManager(this@AssetDetailActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupObservers() {
        viewModel.mutationsState.observe(this) { state ->
            when (state) {
                is MutationsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyStateMutations.visibility = View.GONE
                }
                is MutationsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.transactions
                    if (list.isEmpty()) {
                        binding.emptyStateMutations.visibility = View.VISIBLE
                        transactionAdapter.submitList(emptyList())
                    } else {
                        binding.emptyStateMutations.visibility = View.GONE
                        transactionAdapter.submitList(list)
                    }
                }
                is MutationsState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMutations() {
        viewModel.getMutations(assetId)
    }
    
    override fun onResume() {
        super.onResume()
        // If we came back from editing, we should ideally refresh. 
        // For simplicity, just reload mutations for now.
        loadMutations()
    }
}
