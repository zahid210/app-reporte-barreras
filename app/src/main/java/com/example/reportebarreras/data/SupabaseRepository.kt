package com.example.reportebarreras.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

object SupabaseRepository {

    // Usamos el cliente centralizado desde nuestro objeto Singleton
    private val client = SupabaseClient.client

    // --- AGREGAR ESTO ---

    // 1. Intentar recuperar la sesión (espera a que Supabase lea el disco)
    suspend fun retrieveUserSession(): String? {
        return try {
            // Esto fuerza a la librería a verificar si hay token guardado
            client.auth.awaitInitialization()
            val user = client.auth.currentUserOrNull()
            user?.email
        } catch (_: Exception) {
            null
        }
    }

    // 2. Cerrar Sesión
    suspend fun logout() {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            println("Error al cerrar sesión: ${e.message}")
        }
    }

    // LOGIN OFICIAL
    suspend fun login(email: String, password: String): Boolean {
        return try {
            client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password.trim()
            }
            true // Si no lanza excepción, el login fue exitoso
        } catch (e: Exception) {
            println("ERROR LOGIN AUTH: ${e.message}")
            false
        }
    }

    // REGISTRO OFICIAL
    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password.trim()
            }
            true
        } catch (e: Exception) {
            println("ERROR SIGNUP AUTH: ${e.message}")
            false
        }
    }

    // OBTENER EMAIL DEL USUARIO ACTUAL (Para pasar a Screen2)
    fun getCurrentUserEmail(): String? {
        return client.auth.currentSessionOrNull()?.user?.email
    }

    suspend fun uploadImage(bytes: ByteArray): String {
        return try {
            val fileName = "reporte_${System.currentTimeMillis()}.jpg"
            val bucket = client.storage.from("reportes")

            // Subir la imagen
            bucket.upload(path = fileName, data = bytes)

            // ✅ USAR EL METODO OFICIAL PARA OBTENER LA URL PÚBLICA
            val publicUrl = bucket.publicUrl(fileName)

            println("DEBUG: URL generada correctamente: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            println("ERROR UPLOAD STORAGE: ${e.message}")
            "" // Si devuelve vacío, el insert fallará o irá vacío
        }
    }

    suspend fun saveReporte(
        userEmail: String,
        descripcion: String,
        fotoUrl: String,
        latitude: Double?,
        longitude: Double?,
        mapsUrl: String?
    ) {
        try {
            val dataToSend = mutableMapOf(
                "user_email" to userEmail,
                "descripcion" to descripcion,
                "foto_url" to fotoUrl
            )

            if (latitude != null && longitude != null) {
                dataToSend["latitude"] = latitude.toString()
                dataToSend["longitude"] = longitude.toString()
            }

            if (mapsUrl != null) {
                dataToSend["maps_url"] = mapsUrl
            }

            client.postgrest["reportes"].insert(dataToSend)
            println("DEBUG: Reporte insertado correctamente")
        } catch (e: Exception) {
            println("ERROR SAVE REPORTE: ${e.message}")
        }
    }
}