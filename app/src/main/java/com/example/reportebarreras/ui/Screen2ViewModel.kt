package com.example.reportebarreras.ui

import android.graphics.Bitmap
import android.location.Location
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportebarreras.data.SupabaseRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class Screen2ViewModel : ViewModel() {

    // Estados de UI
    var bitmap by mutableStateOf<Bitmap?>(null)
    var descripcion by mutableStateOf("")
    var isUploading by mutableStateOf(false)
    var isListening by mutableStateOf(false)

    // Estados de Sensores y Permisos
    var currentLocation by mutableStateOf<Location?>(null)
    var gpsEnabled by mutableStateOf(false)
    var locationPermissionGranted by mutableStateOf(false)
    var cameraPermissionGranted by mutableStateOf(false)
    var audioPermissionGranted by mutableStateOf(false)

    fun onDescripcionChange(newValue: String) {
        descripcion = newValue
    }

    fun resetPhoto() {
        bitmap = null
        descripcion = ""
    }

    fun enviarReporte(userEmail: String, onSuccess: () -> Unit) {
        val currentBitmap = bitmap ?: return
        viewModelScope.launch {
            isUploading = true
            try {
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)

                val url = SupabaseRepository.uploadImage(stream.toByteArray())

                // Dentro de enviarReporte en Screen2ViewModel.kt
                val mapsUrl = currentLocation?.let {
                    "https://www.google.com/maps?q=${it.latitude},${it.longitude}"
                }

                SupabaseRepository.saveReporte(
                    userEmail = userEmail,
                    descripcion = descripcion,
                    fotoUrl = url,
                    latitude = currentLocation?.latitude,
                    longitude = currentLocation?.longitude,
                    mapsUrl = mapsUrl
                )

                resetPhoto()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isUploading = false
            }
        }
    }
}