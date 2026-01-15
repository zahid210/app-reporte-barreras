package com.example.reportebarreras.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reportebarreras.data.SupabaseRepository
import com.example.reportebarreras.ui.Screen1
import com.example.reportebarreras.ui.Screen1UI
import com.example.reportebarreras.ui.Screen2
import com.example.reportebarreras.ui.Screen2UI
import com.example.reportebarreras.ui.Screen3
import com.example.reportebarreras.ui.Screen3UI

@Composable
fun AppNavigation(navController: NavHostController) {
    var isLoadingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Screen1.ROUTE) }

    LaunchedEffect(Unit) {
        val email = SupabaseRepository.retrieveUserSession()
        startDestination = if (email != null) {
            Screen2.createRoute(email)
        } else {
            Screen1.ROUTE
        }
        isLoadingSession = false
    }

    // Usamos AnimatedContent para que el paso de "Carga" a "NavHost" sea suave
    AnimatedContent(
        targetState = isLoadingSession,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "SplashToMainTransition"
    ) { loading ->
        if (loading) {
            // ACTUALIZADO: Usamos el color del tema para evitar flashazos en Modo Oscuro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                // --- PANTALLA 1: LOGIN ---
                composable(Screen1.ROUTE) {
                    Screen1UI(
                        onGoToScreen2 = { email ->
                            navController.navigate(Screen2.createRoute(email)) {
                                popUpTo(Screen1.ROUTE) { inclusive = true }
                            }
                        }
                    )
                }

                // --- PANTALLA 2: PRINCIPAL (Reportar) ---
                composable(
                    route = Screen2.ROUTE,
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""

                    Screen2UI(
                        userEmail = email,
                        onLogout = {
                            navController.navigate(Screen1.ROUTE) {
                                popUpTo(0)
                            }
                        },
                        // NUEVO: Navegación hacia el historial de reportes
                        onGoToHistory = {
                            navController.navigate(Screen3.createRoute(email))
                        }
                    )
                }

                // --- PANTALLA 3: HISTORIAL DE REPORTES ---
                composable(
                    route = Screen3.ROUTE,
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""

                    Screen3UI(
                        userEmail = email,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}