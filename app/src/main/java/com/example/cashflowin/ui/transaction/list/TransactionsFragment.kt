package com.example.cashflowin.ui.transaction.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.databinding.FragmentTransactionsBinding
import com.example.cashflowin.ui.dashboard.TransactionAdapter
import com.example.cashflowin.ui.transaction.TransactionViewModelFactory
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels {
        val apiService = ApiClient.getApiService(requireContext())
        val repository = TransactionRepository(apiService)
        TransactionViewModelFactory(repository)
    }
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var tokenManager: TokenManager

    private var currentFilterType: String? = null
    private var currentSearch: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tokenManager = TokenManager(requireContext())

        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupDatePicker()
        setupObservers()

        fetchTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            val intent = android.content.Intent(requireContext(), com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java).apply {
                putExtra("EXTRA_ID", transaction.id)
                putExtra("EXTRA_AMOUNT", transaction.amount)
                putExtra("EXTRA_TYPE", transaction.type)
                putExtra("EXTRA_DESC", transaction.description)
                putExtra("EXTRA_DATE", transaction.date)
                putExtra("EXTRA_CATEGORY_ID", transaction.category_id)
                putExtra("EXTRA_ASSET_ID", transaction.asset_id)
            }
            startActivity(intent)
        }
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayoutFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilterType = when (tab?.position) {
                    0 -> null // All
                    1 -> "income"
                    2 -> "expense"
                    else -> null
                }
                applyFilters()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    currentSearch = if (s.isNullOrBlank()) null else s.toString()
                    applyFilters()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupDatePicker() {
        binding.btnDateFilter.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .setSelection(
                    Pair(
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                startDate = sdf.format(Date(selection.first))
                endDate = sdf.format(Date(selection.second))
                
                binding.tvActiveDateFilter.text = "Showing: $startDate to $endDate"
                applyFilters()
            }
            
            datePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
        }
        
        binding.btnDateFilter.setOnLongClickListener {
            startDate = null
            endDate = null
            binding.tvActiveDateFilter.text = "Showing: All dates"
            applyFilters()
            true
        }
    }

    private fun setupObservers() {
        viewModel.transactionsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TransactionsState.Idle -> {}
                is TransactionsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }
                is TransactionsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.transactions
                    
                    if (list.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        transactionAdapter.submitList(emptyList())
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        transactionAdapter.submitList(list)
                    }
                }
                is TransactionsState.Error -> {
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

    private fun fetchTransactions() {
        viewModel.loadTransactions(
            type = currentFilterType,
            search = currentSearch,
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun applyFilters() {
        // If we already have the full list loaded, we can filter locally for instant feedback
        viewModel.filterTransactionsLocally(
            type = currentFilterType,
            search = currentSearch,
            startDate = startDate,
            endDate = endDate
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
