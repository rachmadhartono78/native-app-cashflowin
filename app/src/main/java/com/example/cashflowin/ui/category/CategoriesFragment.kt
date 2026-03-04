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
import com.example.cashflowin.databinding.FragmentCategoriesBinding
import com.example.cashflowin.utils.TokenManager

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels {
        val apiService = ApiClient.getApiService(requireContext())
        val repository = CategoryRepository(apiService)
        CategoryViewModelFactory(repository)
    }
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var tokenManager: TokenManager

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

        tokenManager = TokenManager(requireContext())

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            val intent = Intent(requireContext(), AddEditCategoryActivity::class.java).apply {
                putExtra("EXTRA_CATEGORY_ID", category.id)
                putExtra("EXTRA_CATEGORY_NAME", category.name)
                putExtra("EXTRA_CATEGORY_TYPE", category.type)
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
    }

    private fun setupObservers() {
        viewModel.categoriesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CategoriesState.Idle -> {}
                is CategoriesState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                }
                is CategoriesState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.response.data
                    
                    if (list.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        categoryAdapter.submitList(emptyList())
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        // Sort by type to group them (Income first, then Expense)
                        val sortedList = list.sortedByDescending { it.type }
                        categoryAdapter.submitList(sortedList)
                    }
                }
                is CategoriesState.Error -> {
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

    private fun fetchCategories() {
        viewModel.loadCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
