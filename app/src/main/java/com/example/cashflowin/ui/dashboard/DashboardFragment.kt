package com.example.cashflowin.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflowin.R
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.DashboardRepository
import com.example.cashflowin.api.model.Summary
import com.example.cashflowin.databinding.FragmentDashboardBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.ui.transaction.ScanNotaActivity
import com.example.cashflowin.utils.TokenManager
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            requireActivity().application,
            DashboardRepository(ApiClient.getApiService(requireContext()))
        )
    }
    private lateinit var transactionAdapter: TransactionAdapter
    
    private var isBalanceVisible = true
    private var currentSummary: Summary? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        if (tokenManager.getToken() == null) {
            navigateToLogin()
            return
        }

        updateGreeting()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 0..10 -> getString(R.string.greeting_morning)
            in 11..14 -> getString(R.string.greeting_afternoon)
            in 15..18 -> getString(R.string.greeting_evening)
            else -> getString(R.string.greeting_night)
        }
        
        binding.tvGreeting.text = "$greeting,"
        
        val userName = tokenManager.getUserName() ?: "User"
        binding.tvUserName.text = userName
    }

    private fun setupListeners() {
        // Foto profil bisa diklik untuk pindah ke tab Settings
        binding.btnProfile.setOnClickListener {
            try {
                findNavController().navigate(R.id.nav_settings)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Membuka Profil...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCatat.setOnClickListener {
            openAddTransaction()
        }

        binding.btnAddTransactionFab.setOnClickListener {
            openAddTransaction()
        }

        binding.btnBudgets.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.BudgetsActivity::class.java))
        }

        binding.btnGoals.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.GoalsActivity::class.java))
        }

        binding.btnDebts.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.DebtsActivity::class.java))
        }

        binding.btnCalendar.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.CalendarActivity::class.java))
        }

        binding.btnFinancialCalculator.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.CalculatorActivity::class.java))
        }

        binding.btnScanNota.setOnClickListener {
            startActivity(Intent(requireContext(), ScanNotaActivity::class.java))
        }

        binding.btnAiSuggestion.setOnClickListener {
            Toast.makeText(requireContext(), "AI Advisor akan menganalisa keuanganmu...", Toast.LENGTH_LONG).show()
            // Placeholder untuk fitur AI nantinya
        }

        binding.btnMore.setOnClickListener {
            Toast.makeText(requireContext(), "Segera Hadir!", Toast.LENGTH_SHORT).show()
        }

        binding.btnHideBalance.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            currentSummary?.let { updateUI(it) }
            
            val iconRes = if (isBalanceVisible) {
                android.R.drawable.ic_menu_view
            } else {
                android.R.drawable.ic_menu_close_clear_cancel 
            }
            binding.btnHideBalance.setImageResource(iconRes)
        }
        
        binding.tvViewAll.setOnClickListener {
            // Pindah ke tab Transaksi
            try {
                findNavController().navigate(R.id.nav_transactions)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.view_all), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAddTransaction() {
        startActivity(Intent(requireContext(), com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (tokenManager.getToken() != null) {
            viewModel.loadDashboardData()
            updateGreeting()
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            val intent = Intent(requireContext(), com.example.cashflowin.ui.transaction.AddTransactionActivity::class.java).apply {
                putExtra("EXTRA_ID", transaction.id)
                putExtra("EXTRA_AMOUNT", transaction.amount)
                putExtra("EXTRA_TYPE", transaction.type)
                putExtra("EXTRA_DESC", transaction.description)
                putExtra("EXTRA_DATE", transaction.date)
                putExtra("EXTRA_CATEGORY_ID", transaction.category?.id)
                putExtra("EXTRA_ASSET_ID", transaction.asset?.id)
            }
            startActivity(intent)
        }
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardState.Loading -> setLoading(true)
                is DashboardState.Success -> {
                    setLoading(false)
                    val summary = state.response.data?.summary
                    val transactions = state.response.data?.recent_transactions ?: emptyList()

                    currentSummary = summary
                    summary?.let { updateUI(it) }
                    transactionAdapter.submitList(transactions)
                }
                is DashboardState.Error -> {
                    setLoading(false)
                    if (state.message == "UNAUTHORIZED") {
                        tokenManager.clearToken()
                        navigateToLogin()
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        // Simple loading for now
    }

    private fun updateUI(summary: Summary) {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
        
        if (isBalanceVisible) {
            binding.tvTotalBalance.text = format.format(summary.balance)
            binding.tvIncome.text = format.format(summary.total_income_month)
            binding.tvExpense.text = format.format(summary.total_expense_month)
        } else {
            val hiddenText = getString(R.string.balance_hidden)
            binding.tvTotalBalance.text = hiddenText
            binding.tvIncome.text = hiddenText
            binding.tvExpense.text = hiddenText
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
