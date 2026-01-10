package com.example.reportebarreras.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import com.example.reportebarreras.R

// Ruta tipada para navegación
@Serializable
object Screen1 {
    const val ROUTE = "screen1"
}

@Composable
fun Screen1UI(
    onGoToScreen2: (userEmail: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Screen1ViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE6E7E8))
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 1. Logos (Bloque Superior) ---
            Image(
                painter = painterResource(id = R.drawable.logo_uncp),
                contentDescription = "logo_UNCP",
                modifier = Modifier.width(280.dp).height(65.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp)) // Espacio generoso entre logos

            Image(
                painter = painterResource(id = R.drawable.logo_fis),
                contentDescription = "logo_FIS",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            // --- 2. Separador Central ---
            // Este es el "colchón" principal entre la identidad y el formulario
            Spacer(modifier = Modifier.height(60.dp))

            // --- 3. Inputs (Formulario) ---
            BottomBorderInput(
                value = viewModel.user,
                onValueChange = { viewModel.onUserChange(it) },
                placeholder = "Usuario",
                isEmail = true,
                heightDp = 50.dp,
                innerPaddingDp = 6.dp,
                leadingIcon = Icons.Filled.Person
            )

            Spacer(modifier = Modifier.height(25.dp)) // Espacio entre inputs

            BottomBorderInput(
                value = viewModel.pass,
                onValueChange = { viewModel.onPassChange(it) },
                placeholder = "Contraseña",
                isPassword = true,
                heightDp = 50.dp,
                innerPaddingDp = 6.dp,
                leadingIcon = Icons.Filled.Lock
            )

            // --- 4. Bloque de Error (Seguridad Anti-Superposición) ---
            // Usamos un padding vertical en lugar de altura fija para que el texto
            // tenga su propio espacio vital y "empuje" suavemente si es muy largo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp), // Margen de seguridad arriba y abajo
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.errorMessage.isNotEmpty()) {
                    Text(
                        text = viewModel.errorMessage,
                        color = Color(0xFFB83B41),
                        fontSize = 13.sp,
                        lineHeight = 16.sp, // Mejor lectura si el error tiene 2 líneas
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    // Mantenemos un espacio invisible cuando no hay error
                    // para que el botón no salte bruscamente al aparecer el texto
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- 5. Acciones (Botones) ---
            CustomButtonWithEffect(
                onClick = {
                    viewModel.login(onLoginSuccess = { email ->
                        onGoToScreen2(email)
                    })
                },
                text = if (viewModel.isLoading) "CARGANDO..." else "INGRESAR",
                enabled = !viewModel.isLoading
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (!viewModel.isLoading) {
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.register() }
                ) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // Espacio final para evitar que el botón "Regístrate" pegue al borde
            Spacer(modifier = Modifier.height(20.dp).navigationBarsPadding())
        }
    }
}