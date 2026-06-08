package com.example.reportebarreras.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportebarreras.data.SupabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class Screen2ViewModel : ViewModel() {

    var photoPath by mutableStateOf<String?>(null)
    var descripcion by mutableStateOf("")
    var isUploading by mutableStateOf(false)
    var isListening by mutableStateOf(false)
    var currentLocation by mutableStateOf<Location?>(null)
    var gpsEnabled by mutableStateOf(false)
    var locationPermissionGranted by mutableStateOf(false)
    var cameraPermissionGranted by mutableStateOf(false)
    var audioPermissionGranted by mutableStateOf(false)

    fun onDescripcionChange(newValue: String) {
        descripcion = newValue
    }

    fun resetPhoto() {
        photoPath = null
        descripcion = ""
    }

    fun enviarReporte(userEmail: String, onSuccess: () -> Unit) {
        val currentPath = photoPath ?: return
        viewModelScope.launch {
            isUploading = true
            try {
                val url = withContext(Dispatchers.IO) {
                    val bitmap = BitmapFactory.decodeFile(currentPath)
                    val stream = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    bitmap?.recycle()
                    SupabaseRepository.uploadImage(stream.toByteArray())
                }

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