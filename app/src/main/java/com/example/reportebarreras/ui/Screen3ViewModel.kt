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

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var hasMore by mutableStateOf(true)
        private set

    private var currentPage = 0
    private var loadedEmail: String? = null
    private val pageSize = 20

    fun cargarReportes(userEmail: String, refresh: Boolean = false) {
        if (!refresh && loadedEmail == userEmail && reportes.isNotEmpty() && !hasMore) return
        if (refresh) {
            currentPage = 0
            reportes = emptyList()
            hasMore = true
            loadedEmail = userEmail
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val results = SupabaseRepository.getReportesByUser(
                    email = userEmail,
                    limit = pageSize,
                    offset = currentPage * pageSize
                )
                if (results.size < pageSize) hasMore = false
                reportes = reportes + results
                currentPage++
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "No se pudieron cargar los reportes."
            } finally {
                isLoading = false
            }
        }
    }
}