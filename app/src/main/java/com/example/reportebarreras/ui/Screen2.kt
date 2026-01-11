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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
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
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    // Animación para el micro
    val alpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = ""
    )

    // --- LÓGICA DE PERMISOS Y GPS ---
    val gpsSettingsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            vm.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
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
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
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
                val request =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L).build()

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
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                vm.isListening = true
            }

            override fun onResults(res: Bundle?) {
                res?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    vm.descripcion = if (vm.descripcion.isBlank()) it else "${vm.descripcion}\n$it"
                }
                vm.isListening = false
                recognizer.destroy()
            }

            override fun onError(p0: Int) {
                vm.isListening = false; recognizer.destroy()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    // --- UI RESPONSIVA ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 12.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() }
    ) {
        // ZONA DE CONTENIDO
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- HEADER ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hola,", fontSize = 14.sp, color = Color.DarkGray)
                    Text(
                        text = userEmail.substringBefore("@"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        vm.isUploading = true
                        SupabaseRepository.logout()
                        onLogout()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar Sesión",
                        tint = Color(0xFFB83B41)
                    )
                }
            }

            // ---------- GPS ----------
            AssistChip(
                onClick = {},
                shape = RoundedCornerShape(25),
                border = BorderStroke(1.dp, Color(0xFFB83B41)),
                label = {
                    Text(
                        text = when {
                            !vm.locationPermissionGranted -> "GPS sin permiso"
                            !vm.gpsEnabled -> "GPS apagado"
                            vm.currentLocation != null -> "GPS activo"
                            else -> "Buscando GPS…"
                        },
                        fontSize = 12.sp,
                        color = Color.White
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFB83B41),
                    labelColor = Color.White
                )
            )

            // ---------- CÁMARA ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (vm.bitmap == null && vm.cameraPermissionGranted) {
                    CameraPreviewComponent(Modifier.fillMaxSize(), imageCapture)
                } else if (vm.bitmap != null) {
                    Image(
                        bitmap = vm.bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Permiso de cámara requerido")
                    }
                }
            }

            // ---------- BOTONES CÁMARA ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    enabled = vm.bitmap != null,
                    onClick = { vm.resetPhoto() },
                    shape = RoundedCornerShape(25),
                    border = BorderStroke(1.dp, Color(0xFFB83B41)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB83B41))
                ) { Text("Repetir") }

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    enabled = vm.bitmap == null && vm.cameraPermissionGranted,
                    shape = RoundedCornerShape(25),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83B41)),
                    onClick = {
                        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                        imageCapture.takePicture(
                            ImageCapture.OutputFileOptions.Builder(file).build(),
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(res: ImageCapture.OutputFileResults) {
                                    vm.bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                }

                                override fun onError(e: ImageCaptureException) {
                                    e.printStackTrace()
                                }
                            }
                        )
                    }
                ) { Text("Tomar foto") }
            }

            // ---------- DESCRIPCIÓN ----------
            OutlinedTextField(
                value = vm.descripcion,
                onValueChange = { vm.onDescripcionChange(it) },
                placeholder = { Text("Describe la barrera...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFFB83B41),
                    unfocusedIndicatorColor = Color.Gray,
                    cursorColor = Color(0xFFB83B41),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )

            // ---------- MIC ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { focusManager.clearFocus(); vibrateShort(context); startVoiceInput() },
                    enabled = vm.audioPermissionGranted
                ) {
                    Icon(
                        Icons.Default.Mic,
                        null,
                        tint = if (vm.isListening) Color(0xFFB83B41) else Color.Gray
                    )
                }
                if (vm.isListening) {
                    Text(
                        "Escuchando...",
                        modifier = Modifier.alpha(alpha),
                        color = Color(0xFFB83B41)
                    )
                }
            }
        }

        // PIE DE PÁGINA
        Spacer(modifier = Modifier.weight(0.05f))

        Button(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(48.dp)
                .align(Alignment.CenterHorizontally),
            enabled = vm.bitmap != null && vm.descripcion.isNotBlank() && !vm.isUploading,
            shape = RoundedCornerShape(25),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83B41)),
            onClick = { vm.enviarReporte(userEmail) { } }
        ) {
            Text(
                if (vm.isUploading) "Enviando..." else "Enviar reporte",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(0.05f))
    }
}