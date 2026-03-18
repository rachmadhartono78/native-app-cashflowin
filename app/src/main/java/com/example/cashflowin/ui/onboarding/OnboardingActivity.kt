package com.example.cashflowin.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.cashflowin.MainActivity
import com.example.cashflowin.R
import com.example.cashflowin.databinding.ActivityOnboardingBinding
import com.example.cashflowin.utils.TokenManager
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        tokenManager = TokenManager(this)
        
        // Cek jika bukan pertama kali, langsung ke MainActivity (yang nanti akan cek Login)
        if (!tokenManager.isFirstTime()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val onboardingItems = listOf(
            OnboardingItem(
                R.drawable.ic_nav_dashboard,
                "Atur Keuanganmu",
                "Catat setiap pemasukan dan pengeluaranmu dengan mudah dan cepat."
            ),
            OnboardingItem(
                R.drawable.ic_nav_categories,
                "Kategori Transaksi",
                "Kelola kategori transaksi sesuai dengan kebutuhan gaya hidupmu."
            ),
            OnboardingItem(
                R.drawable.ic_nav_assets,
                "Pantau Asetmu",
                "Lihat perkembangan asetmu secara real-time dalam satu aplikasi."
            )
        )

        val adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onboardingItems.size - 1) {
                    binding.btnNext.text = "Mulai Sekarang"
                } else {
                    binding.btnNext.text = "Lanjut"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < onboardingItems.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        tokenManager.setFirstTime(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}