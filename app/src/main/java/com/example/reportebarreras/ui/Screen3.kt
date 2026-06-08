package com.example.reportebarreras.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable

@Serializable
object Screen3 {
    const val ROUTE = "screen3/{email}"
    fun createRoute(email: String) = "screen3/$email"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen3UI(
    userEmail: String,
    onBack: () -> Unit,
    vm: Screen3ViewModel = viewModel()
) {
    val listState = rememberLazyListState()

    LaunchedEffect(userEmail) {
        vm.cargarReportes(userEmail, refresh = true)
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= listState.layoutInfo.totalItemsCount - 3
                    && vm.hasMore && !vm.isLoading
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            vm.cargarReportes(userEmail)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Reportes",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when {
                vm.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semantics { contentDescription = "Cargando reportes..." },
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                vm.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semantics { contentDescription = vm.errorMessage ?: "Error" },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = vm.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { vm.cargarReportes(userEmail) }) {
                            Text("Reintentar")
                        }
                    }
                }

                vm.reportes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Aún no has enviado ningún reporte.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {
                        items(vm.reportes) { reporte ->
                            ReporteCard(reporte = reporte)
                        }
                        if (vm.hasMore || vm.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .semantics { contentDescription = "Cargando más reportes..." },
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}