package com.example.reportebarreras.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportebarreras.data.Reporte
import com.example.reportebarreras.data.SupabaseRepository
import kotlinx.coroutines.launch

class Screen3ViewModel : ViewModel() {

    var reportes by mutableStateOf<List<Reporte>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun cargarReportes(userEmail: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                reportes = SupabaseRepository.getReportesByUser(userEmail)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "No se pudieron cargar los reportes."
            } finally {
                isLoading = false
            }
        }
    }
}