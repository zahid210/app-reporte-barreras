package com.example.reportebarreras.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.*
import kotlinx.serialization.Serializable
import java.io.File
import com.example.reportebarreras.data.SupabaseRepository
import kotlinx.coroutines.launch

@Serializable
object Screen2 {
    const val ROUTE = "screen2/{email}"
    fun createRoute(email: String) = "screen2/$email"
}

@Composable
fun Screen2UI(
    userEmail: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    vm: Screen2ViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }

    // Animación para el micro
    val alpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = ""
    )

    // --- LÓGICA DE PERMISOS Y GPS ---
    val gpsSettingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        vm.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        vm.locationPermissionGranted = p[Manifest.permission.ACCESS_FINE_LOCATION] == true
        vm.cameraPermissionGranted = p[Manifest.permission.CAMERA] == true
        vm.audioPermissionGranted = p[Manifest.permission.RECORD_AUDIO] == true

        if (vm.locationPermissionGranted) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            vm.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!vm.gpsEnabled) gpsSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    LaunchedEffect(Unit) {
        permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    // Actualizaciones de ubicación
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                vm.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    // --- LÓGICA DE ACTUALIZACIÓN DE UBICACIÓN (CORREGIDA) ---
    LaunchedEffect(vm.locationPermissionGranted, vm.gpsEnabled) {
        if (vm.locationPermissionGranted && vm.gpsEnabled) {

            // VERIFICACIÓN EXPLÍCITA DE PERMISOS (Para evitar el error de compilación)
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L).build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(res: LocationResult) {
                        vm.currentLocation = res.lastLocation
                    }
                }

                try {
                    fusedLocationClient.requestLocationUpdates(
                        request,
                        callback,
                        context.mainLooper
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- FUNCIONES INTERNAS (VOICE / FOTO) ---
    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) { vm.isListening = true }
            override fun onResults(res: Bundle?) {
                res?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    vm.descripcion = if (vm.descripcion.isBlank()) it else "${vm.descripcion}\n$it"
                }
                vm.isListening = false
                recognizer.destroy()
            }
            override fun onError(p0: Int) { vm.isListening = false; recognizer.destroy() }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    // --- UI ---
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF2F2F2)).statusBarsPadding().padding(24.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { focusManager.clearFocus() },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- NUEVA FILA SUPERIOR: Email + Botón Salir ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hola, ${userEmail.substringBefore("@")}", // Solo muestra nombre
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.Black
            )

            IconButton(onClick = {
                scope.launch {
                    vm.isUploading = true // Opcional: para bloquear botones
                    SupabaseRepository.logout() // 1. Cerrar en Supabase
                    onLogout() // 2. Navegar al Login
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    tint = Color(0xFFB83B41)
                )
            }
        }

        // Chip GPS
        AssistChip(
            onClick = {},
            label = { Text(when { !vm.locationPermissionGranted -> "GPS sin permiso"; !vm.gpsEnabled -> "GPS apagado"; vm.currentLocation != null -> "GPS activo"; else -> "Buscando GPS…" }, fontSize = 12.sp) },
            colors = AssistChipDefaults.assistChipColors(labelColor = if (vm.currentLocation != null) Color(0xFF2E7D32) else Color(0xFFC62828))
        )

        // Card de Cámara
        Card(modifier = Modifier.fillMaxWidth().height(320.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            if (vm.bitmap == null && vm.cameraPermissionGranted) {
                CameraPreviewComponent(Modifier.fillMaxSize(), imageCapture)
            } else if (vm.bitmap != null) {
                Image(bitmap = vm.bitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Permiso de cámara requerido") }
            }
        }

        // Botones Cámara
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                modifier = Modifier.weight(1f).height(45.dp),
                enabled = vm.bitmap != null,
                onClick = { vm.resetPhoto() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB83B41))
            ) { Text("Repetir") }

            Button(
                modifier = Modifier.weight(1f).height(45.dp),
                enabled = vm.bitmap == null && vm.cameraPermissionGranted,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83B41)),
                onClick = {
                    val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                    imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(res: ImageCapture.OutputFileResults) { vm.bitmap = BitmapFactory.decodeFile(file.absolutePath) }
                            override fun onError(e: ImageCaptureException) { e.printStackTrace() }
                        })
                }
            ) { Text("Tomar foto") }
        }

        // Texto Descriptivo
        OutlinedTextField(
            value = vm.descripcion,
            onValueChange = { vm.onDescripcionChange(it) },
            placeholder = { Text("Describe la barrera…") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFB83B41), cursorColor = Color(0xFFB83B41))
        )

        // Fila Micrófono
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { focusManager.clearFocus(); vibrateShort(context); startVoiceInput() }, enabled = vm.audioPermissionGranted) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = if (vm.isListening) Color(0xFFB83B41) else Color.Gray)
            }
            if (vm.isListening) {
                Text("Escuchando…", modifier = Modifier.alpha(alpha), color = Color(0xFFB83B41))
            }
        }

        // Botón Enviar
        Button(
            modifier = Modifier.fillMaxWidth(0.5f).height(45.dp).align(Alignment.CenterHorizontally),
            enabled = vm.bitmap != null && vm.descripcion.isNotBlank() && !vm.isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83B41)),
            onClick = { vm.enviarReporte(userEmail) { /* Éxito */ } }
        ) {
            Text(if (vm.isUploading) "Enviando…" else "Enviar reporte")
        }
    }
}