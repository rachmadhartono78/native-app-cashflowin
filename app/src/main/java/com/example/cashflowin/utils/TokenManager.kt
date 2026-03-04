package com.example.cashflowin.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cashflowin_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("USER_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("USER_TOKEN", null)
    }

    fun clearToken() {
        prefs.edit().remove("USER_TOKEN").apply()
    }
}
