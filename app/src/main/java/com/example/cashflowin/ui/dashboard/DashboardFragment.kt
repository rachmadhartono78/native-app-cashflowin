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
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.Summary
import com.example.cashflowin.databinding.FragmentDashboardBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.ui.transaction.ScanNotaActivity
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
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
    private var currentAssets: List<AssetInfo> = emptyList()

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

        setupActionButtons()
        updateGreeting()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupActionButtons() {
        binding.btnCatat.tvActionLabel.text = "Catat"
        binding.btnCatat.ivActionIcon.setImageResource(R.drawable.ic_add)
        
        binding.btnBudgets.tvActionLabel.text = "Anggaran"
        binding.btnBudgets.ivActionIcon.setImageResource(android.R.drawable.ic_menu_agenda)
        
        binding.btnGoals.tvActionLabel.text = "Target"
        binding.btnGoals.ivActionIcon.setImageResource(android.R.drawable.ic_menu_compass)
        
        binding.btnDebts.tvActionLabel.text = "Hutang"
        binding.btnDebts.ivActionIcon.setImageResource(android.R.drawable.ic_menu_recent_history)

        binding.btnCalendar.tvActionLabel.text = "Kalender"
        binding.btnCalendar.ivActionIcon.setImageResource(android.R.drawable.ic_menu_today)

        binding.btnFinancialCalculator.tvActionLabel.text = "Kalkulator"
        binding.btnFinancialCalculator.ivActionIcon.setImageResource(android.R.drawable.ic_menu_edit)

        binding.btnScanNota.tvActionLabel.text = "Scan Nota"
        binding.btnScanNota.ivActionIcon.setImageResource(android.R.drawable.ic_menu_camera)

        binding.btnMore.tvActionLabel.text = "Lainnya"
        binding.btnMore.ivActionIcon.setImageResource(android.R.drawable.ic_menu_more)
        
        binding.btnRecurringTransactions.tvActionLabel.text = "Rutin"
        binding.btnRecurringTransactions.ivActionIcon.setImageResource(android.R.drawable.ic_popup_sync)
    }

    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 0..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
        
        binding.tvGreeting.text = "$greeting,"
        
        val userName = tokenManager.getUserName() ?: "User"
        binding.tvUserName.text = userName
    }

    private fun setupListeners() {
        binding.btnProfile.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNav?.selectedItemId = R.id.nav_settings
        }

        binding.btnCatat.root.setOnClickListener { openAddTransaction() }
        binding.btnAddTransactionFab.setOnClickListener { openAddTransaction() }

        binding.btnBudgets.root.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.BudgetsActivity::class.java))
        }

        binding.btnGoals.root.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.GoalsActivity::class.java))
        }

        binding.btnDebts.root.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.DebtsActivity::class.java))
        }

        binding.btnCalendar.root.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.CalendarActivity::class.java))
        }

        binding.btnFinancialCalculator.root.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.CalculatorActivity::class.java))
        }

        binding.btnScanNota.root.setOnClickListener {
            startActivity(Intent(requireContext(), ScanNotaActivity::class.java))
        }

        binding.btnRecurringTransactions.root.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.cashflowin.ui.planning.RecurringTransactionsActivity::class.java))
        }

        binding.btnAiSuggestion.setOnClickListener {
            Toast.makeText(requireContext(), "AI Advisor sedang menganalisa...", Toast.LENGTH_SHORT).show()
        }

        binding.btnMore.root.setOnClickListener {
            binding.btnRecurringTransactions.root.visibility = if (binding.btnRecurringTransactions.root.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        binding.btnHideBalance.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateUI()
            
            val iconRes = if (isBalanceVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel 
            binding.btnHideBalance.setImageResource(iconRes)
        }
        
        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.nav_transactions)
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
                is DashboardState.Loading -> {}
                is DashboardState.Success -> {
                    currentSummary = state.response.data?.summary
                    currentAssets = state.assets
                    updateUI()
                    transactionAdapter.submitList(state.response.data?.recent_transactions ?: emptyList())
                }
                is DashboardState.Error -> {
                    if (state.message == "UNAUTHORIZED") {
                        tokenManager.clearToken()
                        navigateToLogin()
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {}
            }
        }
    }

    private fun updateUI() {
        val summary = currentSummary ?: return
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
        
        val calculatedTotalBalance = currentAssets.sumOf { it.balance }
        
        if (isBalanceVisible) {
            binding.tvTotalBalance.text = format.format(calculatedTotalBalance)
            binding.tvIncome.text = format.format(summary.total_income_month)
            binding.tvExpense.text = format.format(summary.total_expense_month)
            binding.tvSavings.text = format.format(summary.total_savings ?: 0.0)
            binding.tvNetWorth.text = format.format(summary.net_worth ?: 0.0)
        } else {
            val hiddenText = "••••••••"
            binding.tvTotalBalance.text = hiddenText
            binding.tvIncome.text = hiddenText
            binding.tvExpense.text = hiddenText
            binding.tvSavings.text = hiddenText
            binding.tvNetWorth.text = hiddenText
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
