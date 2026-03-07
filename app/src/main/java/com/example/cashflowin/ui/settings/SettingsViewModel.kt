package com.example.cashflowin.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.DashboardRepository
import com.example.cashflowin.api.model.Summary
import com.example.cashflowin.ui.dashboard.DashboardState
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _settingsState = MutableLiveData<DashboardState>()
    val settingsState: LiveData<DashboardState> = _settingsState

    fun loadSettingsData() {
        _settingsState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    _settingsState.value = DashboardState.Success(response.body()!!)
                } else {
                    _settingsState.value = DashboardState.Error(response.message() ?: "Failed to load data")
                }
            } catch (e: Exception) {
                _settingsState.value = DashboardState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
