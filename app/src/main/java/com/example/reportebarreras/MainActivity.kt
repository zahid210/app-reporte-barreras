package com.example.reportebarreras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.reportebarreras.navigation.AppNavigation
import android.view.animation.AlphaAnimation
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Usamos la versión de la librería (androidx.core.splashscreen)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Animación de salida CUIDANDO la compatibilidad
        splashScreen.setOnExitAnimationListener { splashScreenProvider ->
            // En la librería de soporte, se usa splashScreenProvider.view (no .view a secas)
            val view = splashScreenProvider.view

            val fadeOut = AlphaAnimation(1f, 0f).apply {
                duration = 400L
                fillAfter = true
            }

            fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    // Eliminamos el splash usando el provider
                    splashScreenProvider.remove()
                }
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })

            view.startAnimation(fadeOut)
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}