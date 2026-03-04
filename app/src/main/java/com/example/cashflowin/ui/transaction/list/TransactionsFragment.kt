package com.example.cashflowin.ui.transaction.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.databinding.FragmentTransactionsBinding
import com.example.cashflowin.ui.dashboard.TransactionAdapter
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.tabs.TabLayout

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var tokenManager: TokenManager

    private var currentFilterType: String? = null

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
                fetchTransactions()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
                    val list = state.response.data?.data ?: emptyList()
                    
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
                        // Typically handled in MainActivity globally, but we can clear token here too.
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun fetchTransactions() {
        val token = tokenManager.getToken()
        if (token != null) {
            viewModel.loadTransactions(token, currentFilterType)
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
