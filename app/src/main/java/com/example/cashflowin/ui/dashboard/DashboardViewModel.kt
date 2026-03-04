package com.example.cashflowin.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.DashboardRepository
import com.example.cashflowin.api.model.DashboardResponse
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val response: DashboardResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
    object LoggedOut : DashboardState()
    data class ExportComplete(val fileName: String, val message: String) : DashboardState()
}

class DashboardViewModel(
    application: Application,
    private val repository: DashboardRepository
) : AndroidViewModel(application) {
    
    private val _dashboardState = MutableLiveData<DashboardState>(DashboardState.Idle)
    val dashboardState: LiveData<DashboardState> = _dashboardState

    fun loadDashboardData() {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true)) {
                        _dashboardState.value = DashboardState.Success(body)
                    } else {
                        _dashboardState.value = DashboardState.Error(body.message ?: "Failed to fetch data")
                    }
                } else if (response.code() == 401) {
                    _dashboardState.value = DashboardState.Error("UNAUTHORIZED")
                } else {
                    _dashboardState.value = DashboardState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Network error occurred")
            }
        }
    }

    fun exportReportPdf(month: String, year: String, saveFileCallback: (ResponseBody) -> Pair<Boolean, String>) {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val response = repository.exportPdf(month, year)
                if (response.isSuccessful && response.body() != null) {
                    val (success, message) = saveFileCallback(response.body()!!)
                    if (success) {
                        _dashboardState.value = DashboardState.ExportComplete("Laporan.pdf", message)
                    } else {
                        _dashboardState.value = DashboardState.Error("Failed to save PDF: $message")
                    }
                } else {
                    _dashboardState.value = DashboardState.Error("Export failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Network error")
            }
        }
    }

    fun exportReportCsv(month: String, year: String, saveFileCallback: (ResponseBody) -> Pair<Boolean, String>) {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val response = repository.exportCsv(month, year)
                if (response.isSuccessful && response.body() != null) {
                    val (success, message) = saveFileCallback(response.body()!!)
                    if (success) {
                        _dashboardState.value = DashboardState.ExportComplete("Laporan.csv", message)
                    } else {
                        _dashboardState.value = DashboardState.Error("Failed to save CSV: $message")
                    }
                } else {
                    _dashboardState.value = DashboardState.Error("Export failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Network error")
            }
        }
    }
}
