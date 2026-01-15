package com.example.reportebarreras.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importante: Este import ahora funcionará tras agregar la dependencia en gradle
import coil.compose.AsyncImage
import com.example.reportebarreras.data.Reporte
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun ReporteCard(reporte: Reporte) {
    val context = LocalContext.current

    // Formateo de fecha corregido
    val fechaFormateada = remember(reporte.createdAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))

            val date = inputFormat.parse(reporte.createdAt)
            if (date != null) outputFormat.format(date) else reporte.createdAt
        } catch (_: Exception) {
            reporte.createdAt.take(10) // Fallback simple
        }
    }

    // Colores según estado (Usando MaterialTheme para consistencia, pero con tintes específicos)
    val (estadoColor, estadoTextoColor) = when (reporte.estado.lowercase()) {
        "atendido" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Verde
        "en revisión" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)) // Azul
        "rechazado" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // Rojo
        else -> Pair(Color(0xFFFFF8E1), Color(0xFFF9A825)) // Amarillo/Naranja (Pendiente)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // 1. FOTO (Si existe)
            if (!reporte.fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = reporte.fotoUrl,
                    contentDescription = "Foto del reporte",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // 2. CONTENIDO
            Column(modifier = Modifier.padding(16.dp)) {

                // Cabecera: Fecha y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fechaFormateada,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Chip de Estado
                    Surface(
                        color = estadoColor,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = reporte.estado.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = estadoTextoColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = reporte.descripcion ?: "Sin descripción",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de Ubicación
                if (!reporte.mapsUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            // SOLUCIÓN URI: Uri.parse es estándar y correcto.
                            // Si tienes androidx.core-ktx puedes usar .toUri(), pero Uri.parse es más universal.
                            val intent = Intent(Intent.ACTION_VIEW, reporte.mapsUrl.toUri())
                            // Añadimos bandera por seguridad para abrir fuera de la app
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Ver Ubicación en Mapa", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}