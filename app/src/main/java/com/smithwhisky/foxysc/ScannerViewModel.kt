package com.smithwhisky.foxysc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import okhttp3.*
import java.util.*

 data class UiState(
    val portalUrl: String = "",
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val hitsCount: Int = 0,
    val checkedCount: Int = 0,
    val results: List<String> = emptyList()
)

class ScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var job: Job? = null

    fun updatePortalUrl(url: String) {
        _uiState.value = _uiState.value.copy(portalUrl = url)
    }

    fun startScanning() {
        job = viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isScanning = true)
            // MAC scanning logic here (to be fully implemented)
            // Similar to your Python code
        }
    }

    fun stopScanning() {
        job?.cancel()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }
}