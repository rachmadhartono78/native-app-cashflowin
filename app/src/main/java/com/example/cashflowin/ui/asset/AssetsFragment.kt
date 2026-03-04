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
import com.example.cashflowin.databinding.FragmentAssetsBinding
import com.example.cashflowin.utils.TokenManager

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
            val intent = Intent(requireContext(), AddEditAssetActivity::class.java).apply {
                putExtra("EXTRA_ASSET_ID", asset.id)
                putExtra("EXTRA_ASSET_NAME", asset.name)
                putExtra("EXTRA_ASSET_TYPE", asset.type)
                putExtra("EXTRA_ASSET_AMOUNT", asset.balance.toString())
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

    private fun fetchAssets() {
        viewModel.loadAssets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
