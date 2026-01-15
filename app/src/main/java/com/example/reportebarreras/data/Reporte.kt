package com.example.reportebarreras.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reporte(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    val descripcion: String?,
    @SerialName("foto_url") val fotoUrl: String?,
    val estado: String = "Pendiente", // Valor por defecto
    @SerialName("maps_url") val mapsUrl: String?
)