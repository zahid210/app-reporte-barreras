package com.example.reportebarreras.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import com.example.reportebarreras.R

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

    // Usamos Column como contenedor principal para aplicar el recorte del Notch
    Column(
        modifier = modifier
            .fillMaxSize()
            // Aplicamos el color de fondo del tema (Gris claro o Negro)
            .background(MaterialTheme.colorScheme.background)
            // Esto genera el efecto de "corte" en el notch al hacer scroll
            .statusBarsPadding()
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
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_uncp),
                contentDescription = "logo_UNCP",
                modifier = Modifier.width(280.dp).height(65.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_fis),
                contentDescription = "logo_FIS",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(60.dp))

            // --- 3. Inputs (Formulario) ---
            // Nota: Asegúrate de que BottomBorderInput use MaterialTheme.colorScheme.onBackground
            BottomBorderInput(
                value = viewModel.user,
                onValueChange = { viewModel.onUserChange(it) },
                placeholder = "Usuario",
                isEmail = true,
                heightDp = 50.dp,
                innerPaddingDp = 6.dp,
                leadingIcon = Icons.Filled.Person
            )

            Spacer(modifier = Modifier.height(25.dp))

            BottomBorderInput(
                value = viewModel.pass,
                onValueChange = { viewModel.onPassChange(it) },
                placeholder = "Contraseña",
                isPassword = true,
                heightDp = 50.dp,
                innerPaddingDp = 6.dp,
                leadingIcon = Icons.Filled.Lock
            )

            // --- 4. Bloque de Error ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.errorMessage.isNotEmpty()) {
                    Text(
                        text = viewModel.errorMessage,
                        modifier = Modifier.semantics { contentDescription = viewModel.errorMessage },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- 5. Acciones ---
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
                TextButton(
                    onClick = { viewModel.register() }
                ) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        // Color adaptable para el texto secundario
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }

            // Espacio final incluyendo el padding de la barra de navegación
            Spacer(modifier = Modifier.height(20.dp).navigationBarsPadding())
        }
    }
}