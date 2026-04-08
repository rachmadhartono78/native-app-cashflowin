package com.example.cashflowin.api

import android.content.Context
import com.example.cashflowin.BuildConfig
import com.example.cashflowin.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    const val BASE_URL = BuildConfig.BASE_URL

    @Volatile
    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            val tokenManager = TokenManager(context)
            
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                    
                    tokenManager.getToken()?.let {
                        requestBuilder.addHeader("Authorization", "Bearer $it")
                    }
                    
                    val response = chain.proceed(requestBuilder.build())
                    
                    // Auto-logout on token expiration (401 Unauthorized)
                    if (response.code == 401) {
                        tokenManager.clearToken()
                        val intent = android.content.Intent(context, com.example.cashflowin.ui.auth.LoginActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                    
                    response
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            val instance = retrofit.create(ApiService::class.java)
            apiService = instance
            instance
        }
    }
}
