package com.example.reportebarreras.data

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: Int,
    val email: String,
    val password: String
)
