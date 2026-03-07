package com.example.cashflowin.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.DashboardRepository
import com.example.cashflowin.databinding.FragmentSettingsBinding
import com.example.cashflowin.ui.auth.LoginActivity
import com.example.cashflowin.ui.dashboard.DashboardState
import com.example.cashflowin.utils.TokenManager
import java.text.NumberFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager
    
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            DashboardRepository(ApiClient.getApiService(requireContext()))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        setupUI()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSettingsData()
        // Refresh name/email in case they were updated
        binding.tvProfileName.text = tokenManager.getUserName() ?: "User"
    }

    private fun setupUI() {
        binding.tvProfileName.text = tokenManager.getUserName() ?: "User"
    }

    private fun setupObservers() {
        viewModel.settingsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardState.Idle -> {
                    // Initial state
                }
                is DashboardState.Loading -> {
                    // Show small loading if needed
                }
                is DashboardState.Success -> {
                    val summary = state.response.data?.summary
                    summary?.let {
                        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                            maximumFractionDigits = 0
                        }
                        // Update total assets card in settings
                        binding.tvTotalAsset.text = format.format(it.balance)
                    }
                }
                is DashboardState.Error -> {
                    if (state.message == "UNAUTHORIZED") {
                        performLogout()
                    }
                }
                is DashboardState.LoggedOut -> {
                    performLogout()
                }
                is DashboardState.ExportComplete -> {
                    // Not handled in settings
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnResetMonthly.setOnClickListener {
            val intent = Intent(requireContext(), ResetMonthlyTransactionsActivity::class.java)
            startActivity(intent)
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            showPrivacyPolicy()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showPrivacyPolicy() {
        val privacyText = """
            Kebijakan Privasi Cashflowin
            
            1. Penggunaan Kamera:
            Aplikasi ini memerlukan akses kamera untuk fitur 'Scan Nota'. Foto yang diambil hanya diproses secara lokal pada perangkat Anda untuk mengekstrak data transaksi menggunakan teknologi AI/OCR. Kami tidak menyimpan atau mengirimkan foto nota Anda ke server kami.
            
            2. Data Keuangan:
            Data transaksi Anda disimpan dengan aman dan hanya digunakan untuk keperluan pencatatan keuangan pribadi Anda.
            
            3. Keamanan:
            Kami berkomitmen menjaga keamanan data Anda dengan standar industri.
            
            Untuk informasi lebih lengkap, kunjungi: https://cashflowin.kelingstudio.web.id/privacy-policy
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Kebijakan Privasi")
            .setMessage(privacyText)
            .setPositiveButton("Lihat Online") { _, _ ->
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cashflowin.kelingstudio.web.id/privacy-policy"))
                startActivity(browserIntent)
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Keluar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        tokenManager.clearToken()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
