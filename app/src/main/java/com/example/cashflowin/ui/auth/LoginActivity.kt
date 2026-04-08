package com.example.cashflowin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cashflowin.MainActivity
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.AuthRepository
import com.example.cashflowin.databinding.ActivityLoginBinding
import com.example.cashflowin.utils.TokenManager

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(ApiClient.getApiService(this)))
    }
    private lateinit var tokenManager: TokenManager
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        if (tokenManager.getToken() != null) {
            navigateToMain()
        }

        setupGoogleSignIn()
        setupObservers()
        setupListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("666611640044-r8nje9h519nfe57t6pd62qjrdtpnq1ib.apps.googleusercontent.com")
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    android.util.Log.d("OAUTH_DEBUG", "Obtained ID Token: ${token.take(10)}...")
                    viewModel.loginWithGoogle(token)
                } ?: run {
                    android.util.Log.e("OAUTH_DEBUG", "Google ID Token is null!")
                    Toast.makeText(this, "Google ID Token not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                val message = when (e.statusCode) {
                    7 -> "Network Error: Check your internet connection"
                    10 -> "Developer Error: SHA-1 fingerprint mismatch or wrong Client ID"
                    12500 -> "Sign-In Canceled or Configuration Issue"
                    12501 -> "User Cancelled"
                    else -> "Google Sign-In failed (Code ${e.statusCode})"
                }
                android.util.Log.e("OAUTH_DEBUG", "Google Sign-In failed: $message", e)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        } else {
            android.util.Log.w("OAUTH_DEBUG", "Google Sign-In Activity Result not OK: ${result.resultCode}")
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    setLoading(false)
                }
                is AuthState.Loading -> {
                    setLoading(true)
                }
                is AuthState.Success -> {
                    setLoading(false)
                    state.response.token?.let {
                        tokenManager.saveToken(it)
                    }
                    state.response.user?.let {
                        tokenManager.saveUser(it.name, it.email)
                    }
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.tilEmail.isEnabled = !isLoading
        binding.tilPassword.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
