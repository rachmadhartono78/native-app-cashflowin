package com.example.cashflowin.ui.asset

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.databinding.FragmentAssetsBinding
import com.example.cashflowin.utils.TokenManager
import java.text.NumberFormat
import java.util.Locale

class AssetsFragment : Fragment() {

    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssetsViewModel by viewModels {
        val apiService = ApiClient.getApiService(requireContext())
        val repository = AssetRepository(apiService)
        AssetViewModelFactory(repository)
    }
    private lateinit var assetAdapter: AssetAdapter
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchAssets()
    }

    private fun setupRecyclerView() {
        assetAdapter = AssetAdapter { asset ->
            val intent = Intent(requireContext(), AssetDetailActivity::class.java).apply {
                putExtra("EXTRA_ASSET_ID", asset.id)
                putExtra("EXTRA_ASSET_NAME", asset.name)
                putExtra("EXTRA_ASSET_TYPE", asset.type)
                putExtra("EXTRA_ASSET_AMOUNT", asset.balance.toString())
                putExtra("EXTRA_ASSET_COLOR", asset.color)
                putExtra("EXTRA_ASSET_ICON", asset.icon)
            }
            startActivity(intent)
        }
        binding.rvAssets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assetAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddAsset.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditAssetActivity::class.java))
        }

        binding.btnTransferAsset.setOnClickListener {
            startActivity(Intent(requireContext(), TransferAssetActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.assetsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AssetsState.Idle -> {}
                is AssetsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                }
                is AssetsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.response.data
                    
                    updatePortfolioSummary(list)
                    
                    if (list.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        assetAdapter.submitList(emptyList())
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        assetAdapter.submitList(list)
                    }
                }
                is AssetsState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.message == "UNAUTHORIZED") {
                        Toast.makeText(requireContext(), "Session expired.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updatePortfolioSummary(assets: List<AssetInfo>) {
        val totalBalance = assets.sumOf { it.balance }
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
        
        binding.tvTotalValue.text = format.format(totalBalance)
        
        if (totalBalance > 0) {
            val bankBalance = assets.filter { it.type.equals("Bank", true) || it.type.equals("Cash", true) }.sumOf { it.balance }
            val ewalletBalance = assets.filter { it.type.equals("E-Wallet", true) }.sumOf { it.balance }
            val investasiBalance = assets.filter { it.type.equals("Investasi", true) }.sumOf { it.balance }
            val lainnyaBalance = assets.filter { it.type.equals("Lainnya", true) }.sumOf { it.balance }

            binding.tvAllocBank.text = "${(bankBalance / totalBalance * 100).toInt()}%"
            binding.tvAllocEWallet.text = "${(ewalletBalance / totalBalance * 100).toInt()}%"
            binding.tvAllocInvestasi.text = "${(investasiBalance / totalBalance * 100).toInt()}%"
            binding.tvAllocLainnya.text = "${(lainnyaBalance / totalBalance * 100).toInt()}%"
        } else {
            binding.tvAllocBank.text = "0%"
            binding.tvAllocEWallet.text = "0%"
            binding.tvAllocInvestasi.text = "0%"
            binding.tvAllocLainnya.text = "0%"
        }
        
        binding.tvTotalGain.text = "Total ${assets.size} Aset Aktif"
        binding.tvTotalGain.setTextColor(resources.getColor(com.example.cashflowin.R.color.text_secondary, null))
    }

    private fun fetchAssets() {
        viewModel.loadAssets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
