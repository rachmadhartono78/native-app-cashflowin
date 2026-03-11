package com.example.cashflowin.ui.category

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
import com.example.cashflowin.api.CategoryRepository
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.databinding.FragmentCategoriesBinding
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.tabs.TabLayout

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels {
        val apiService = ApiClient.getApiService(requireContext())
        val repository = CategoryRepository(apiService)
        CategoryViewModelFactory(repository)
    }
    private lateinit var categoryAdapter: CategoryAdapter
    private var allCategories: List<CategoryInfo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            val intent = Intent(requireContext(), AddEditCategoryActivity::class.java).apply {
                putExtra("EXTRA_CATEGORY_ID", category.id)
                putExtra("EXTRA_CATEGORY_NAME", category.name)
                putExtra("EXTRA_CATEGORY_TYPE", category.type)
                putExtra("EXTRA_CATEGORY_COLOR", category.color)
                putExtra("EXTRA_CATEGORY_ICON", category.icon)
            }
            startActivity(intent)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditCategoryActivity::class.java))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterCategories(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        viewModel.categoriesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CategoriesState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                }
                is CategoriesState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    allCategories = state.response.data
                    filterCategories(binding.tabLayout.selectedTabPosition)
                }
                is CategoriesState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun filterCategories(tabPosition: Int) {
        val type = if (tabPosition == 0) "expense" else "income"
        val filteredList = allCategories.filter { it.type == type }
        
        if (filteredList.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            categoryAdapter.submitList(emptyList())
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            categoryAdapter.submitList(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}