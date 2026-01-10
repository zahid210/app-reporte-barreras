package com.example.reportebarreras.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            // Ponemos el fondo del mismo color que tu Splash del sistema
            // Esto evita el "flashazo" blanco/negro si Supabase tarda
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE6E7E8))
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen1.ROUTE) {
                    Screen1UI(
                        onGoToScreen2 = { email ->
                            navController.navigate(Screen2.createRoute(email)) {
                                popUpTo(Screen1.ROUTE) { inclusive = true }
                            }
                        }
                    )
                }

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
                        }
                    )
                }
            }
        }
    }
}