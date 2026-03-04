package com.example.cashflowin.ui.dashboard

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.DashboardResponse
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val response: DashboardResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
    object LoggedOut : DashboardState()
    data class ExportComplete(val fileName: String, val message: String) : DashboardState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _dashboardState = MutableLiveData<DashboardState>(DashboardState.Idle)
    val dashboardState: LiveData<DashboardState> = _dashboardState

    fun loadDashboardData(token: String) {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                // Prefix token with "Bearer " as required by Sanctum
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.getDashboardSummary(bearerToken)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _dashboardState.value = DashboardState.Success(body)
                    } else {
                        _dashboardState.value = DashboardState.Error(getApplication<Application>().getString(com.example.cashflowin.R.string.error_fetch_data))
                    }
                } else if (response.code() == 401) {
                    _dashboardState.value = DashboardState.Error(getApplication<Application>().getString(com.example.cashflowin.R.string.error_unauthorized))
                } else {
                    val errorMsg = getApplication<Application>().getString(com.example.cashflowin.R.string.error_server, response.code())
                    _dashboardState.value = DashboardState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(getApplication<Application>().getString(com.example.cashflowin.R.string.error_network))
            }
        }
    }

    fun logout(token: String) {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.logout(bearerToken)
                
                if (response.isSuccessful) {
                    _dashboardState.value = DashboardState.LoggedOut
                } else {
                    val errorMsg = getApplication<Application>().getString(com.example.cashflowin.R.string.error_logout_failed, response.code().toString())
                    _dashboardState.value = DashboardState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(getApplication<Application>().getString(com.example.cashflowin.R.string.error_network))
            }
        }
    }

    fun exportReportPdf(token: String, month: String, year: String, saveFileCallback: (okhttp3.ResponseBody) -> Pair<Boolean, String>) {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.exportPdf(bearerToken, month, year)
                
                if (response.isSuccessful && response.body() != null) {
                    val (success, message) = saveFileCallback(response.body()!!)
                    if (success) {
                        _dashboardState.value = DashboardState.ExportComplete("Laporan.pdf", message)
                    } else {
                        _dashboardState.value = DashboardState.Error("Failed to save downloaded PDF: $message")
                    }
                } else {
                    val errorMsg = getApplication<Application>().getString(com.example.cashflowin.R.string.error_server, response.code())
                    _dashboardState.value = DashboardState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(getApplication<Application>().getString(com.example.cashflowin.R.string.error_network))
            }
        }
    }

    fun exportReportCsv(token: String, month: String, year: String, saveFileCallback: (okhttp3.ResponseBody) -> Pair<Boolean, String>) {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.exportCsv(bearerToken, month, year)
                
                if (response.isSuccessful && response.body() != null) {
                    val (success, message) = saveFileCallback(response.body()!!)
                    if (success) {
                        _dashboardState.value = DashboardState.ExportComplete("Laporan.csv", message)
                    } else {
                        _dashboardState.value = DashboardState.Error("Failed to save downloaded CSV: $message")
                    }
                } else {
                     val errorMsg = getApplication<Application>().getString(com.example.cashflowin.R.string.error_server, response.code())
                    _dashboardState.value = DashboardState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(getApplication<Application>().getString(com.example.cashflowin.R.string.error_network))
            }
        }
    }
}
