package com.example.reportebarreras.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.reportebarreras.data.Reporte
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun ReporteCard(reporte: Reporte) {
    val context = LocalContext.current

    val fechaFormateada = remember(reporte.createdAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))
            val date = inputFormat.parse(reporte.createdAt)
            if (date != null) outputFormat.format(date) else reporte.createdAt
        } catch (_: Exception) {
            reporte.createdAt.take(16).replace("T", " ")
        }
    }

    // Definición de colores según las capturas (Fondos pasteles)
    val (backgroundColor, contentColor) = when (reporte.estado.lowercase()) {
        "atendido" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Verde claro
        "en revisión" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)) // Azul claro
        "rechazado" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // Rojo claro
        else -> Pair(Color(0xFFFFF8E1), Color(0xFFF9A825)) // Amarillo claro (Pendiente)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp) // Diseño plano como en las fotos
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // 1. IMAGEN CUADRADA (Izquierda)
            AsyncImage(
                model = reporte.fotoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. INFORMACIÓN (Derecha)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fechaFormateada,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Badge de Estado
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, contentColor)
                    ) {
                        Text(
                            text = reporte.estado.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reporte.descripcion ?: "Sin descripción...",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN "VER MAPA" pequeño
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, reporte.mapsUrl?.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83B41)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver mapa", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}