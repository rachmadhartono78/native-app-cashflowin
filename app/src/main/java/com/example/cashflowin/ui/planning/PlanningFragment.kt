package com.example.cashflowin.ui.planning

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cashflowin.databinding.FragmentPlanningBinding

class PlanningFragment : Fragment() {

    private var _binding: FragmentPlanningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.cardBudgets.setOnClickListener {
            navigateTo(BudgetsActivity::class.java)
        }

        binding.cardGoals.setOnClickListener {
            navigateTo(GoalsActivity::class.java)
        }

        binding.cardDebts.setOnClickListener {
            navigateTo(DebtsActivity::class.java)
        }

        binding.cardCalculators.setOnClickListener {
            navigateTo(CalculatorActivity::class.java)
        }

        binding.cardCalendar.setOnClickListener {
            navigateTo(CalendarActivity::class.java)
        }
    }

    private fun <T> navigateTo(activityClass: Class<T>) {
        try {
            val intent = Intent(requireContext(), activityClass)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Maaf, fitur ini sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
