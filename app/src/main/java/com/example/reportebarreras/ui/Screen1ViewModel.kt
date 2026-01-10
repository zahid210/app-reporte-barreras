package com.example.reportebarreras.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportebarreras.data.SupabaseRepository
import kotlinx.coroutines.launch

class Screen1ViewModel : ViewModel() {

    // Estados de la UI
    var user by mutableStateOf("")
        private set

    var pass by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf("")
        private set

    // Nuevo estado para mostrar un indicador de carga
    var isLoading by mutableStateOf(false)
        private set

    fun onUserChange(newValue: String) {
        user = newValue
        if (errorMessage.isNotEmpty()) errorMessage = ""
    }

    fun onPassChange(newValue: String) {
        pass = newValue
        if (errorMessage.isNotEmpty()) errorMessage = ""
    }

    // Lógica de Login con Supabase Auth
    fun login(onLoginSuccess: (String) -> Unit) {
        if (user.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor, ingresa email y contraseña"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""

            // Normalizar email
            val emailClean = user.trim().lowercase()

            // Llamada al repositorio (Auth oficial)
            val success = SupabaseRepository.login(emailClean, pass)

            if (success) {
                // Obtenemos el email verificado de la sesión activa
                val verifiedEmail = SupabaseRepository.getCurrentUserEmail() ?: emailClean
                onLoginSuccess(verifiedEmail)
            } else {
                errorMessage = "Error al iniciar sesión. Verifica tus datos o conexión."
            }
            isLoading = false
        }
    }

    // Opcional: Función para Registro (Sign Up)
    fun register() {
        if (user.isBlank() || pass.isBlank()) {
            errorMessage = "Ingresa datos para el registro"
            return
        }
        viewModelScope.launch {
            isLoading = true
            val success = SupabaseRepository.signUp(user.trim().lowercase(), pass)
            errorMessage = if (success) {
                "Registro exitoso. ¡Ya puedes iniciar sesión!"
            } else {
                "Error en el registro. Quizás el usuario ya existe."
            }
            isLoading = false
        }
    }
}