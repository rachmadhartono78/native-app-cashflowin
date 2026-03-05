package com.example.cashflowin.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.UpdatePasswordRequest
import com.example.cashflowin.api.model.UpdateProfileRequest
import com.example.cashflowin.databinding.ActivityEditProfileBinding
import com.example.cashflowin.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        tokenManager = TokenManager(this)

        // Prepopulate Data
        binding.etName.setText(tokenManager.getUserName() ?: "")
        binding.etEmail.setText(tokenManager.getUserEmail() ?: "")

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateProfile(name, email)
        }

        binding.btnUpdatePassword.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePassword(currentPassword, newPassword, confirmPassword)
        }
    }

    private fun updateProfile(name: String, email: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@EditProfileActivity)
                val response = apiService.updateProfile(UpdateProfileRequest(name, email))
                setLoading(false)

                if (response.isSuccessful) {
                    val body = response.body()
                    Toast.makeText(this@EditProfileActivity, body?.message ?: "Profile updated", Toast.LENGTH_SHORT).show()
                    
                    // Update Local Token Manager User detail
                    tokenManager.saveUser(name, email)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.getString("message")
                    } catch (e: Exception) {
                        "Failed to update profile"
                    }
                    Toast.makeText(this@EditProfileActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@EditProfileActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePassword(current: String, new: String, confirm: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@EditProfileActivity)
                val response = apiService.updatePassword(UpdatePasswordRequest(current, new, confirm))
                setLoading(false)

                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    binding.etCurrentPassword.text?.clear()
                    binding.etNewPassword.text?.clear()
                    binding.etConfirmPassword.text?.clear()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.getString("message")
                    } catch (e: Exception) {
                        "Failed to update password"
                    }
                    Toast.makeText(this@EditProfileActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@EditProfileActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnUpdateProfile.isEnabled = !isLoading
        binding.btnUpdatePassword.isEnabled = !isLoading
    }
}
