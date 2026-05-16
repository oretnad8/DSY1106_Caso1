package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.Producto
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.ProductoViewModel

/**
 * Pantalla de Búsqueda de Productos
 * ... (KDoc se mantiene igual)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaScreen(
    productoViewModel: ProductoViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val searchState by productoViewModel.searchState.collectAsStateWithLifecycle()
    val productoDetailState by productoViewModel.productoDetailState.collectAsStateWithLifecycle()
    val searchQuery by productoViewModel.searchQuery.collectAsStateWithLifecycle()
    // val scope = rememberCoroutineScope() // No es necesario aquí, se puede eliminar.

    // Estado local
    var localSearchQuery by remember { mutableStateOf("") }
    var showScanDialog by remember { mutableStateOf(false) } // Este estado no se usa, pero lo dejamos por si acaso.
    var showScanner by remember { mutableStateOf(false) }

    // Navegación basada en eventos únicos del ViewModel
    LaunchedEffect(Unit) {
        productoViewModel.navigationEvent.collect { event ->
            when (event) {
                is com.smartlogix.app.viewmodels.ProductoNavEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.sku)
                }
            }
        }
    }


    Scaffold(
        topBar = {
            BackTopBar(
                title = "Buscar Producto",
                onBackClick = onNavigateBack // Pasamos la acción para volver atrás
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ========== SECCIÓN DE BÚSQUEDA ==========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón grande de cámara
                Card(
                    onClick = {
                        showScanner = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Escanear",
                            modifier = Modifier.size(64.dp),
                            tint = com.smartlogix.app.ui.theme.VibrantBlue
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Escanear Código",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.smartlogix.app.ui.theme.VibrantBlue
                        )

                        Text(
                            text = "Toca para usar la cámara",
                            style = MaterialTheme.typography.bodyMedium,
                            color = com.smartlogix.app.ui.theme.VibrantBlue.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider con texto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "o realizar búsqueda manual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de búsqueda manual
                SearchTextField(
                    value = localSearchQuery,
                    onValueChange = { localSearchQuery = it },
                    placeholder = "SKU, descripción, código de barras",
                    onSearch = {
                        productoViewModel.searchProductos(localSearchQuery)
                    },
                    onClear = {
                        localSearchQuery = ""
                        productoViewModel.clearSearch()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de búsqueda
                PrimaryButton(
                    text = "Buscar",
                    onClick = {
                        productoViewModel.searchProductos(localSearchQuery)
                    },
                    icon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // ========== RESULTADOS DE BÚSQUEDA ==========
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    // Cuando se busca por barcode, el detalle puede cargarse directamente.
                    productoDetailState is UiState.Loading -> {
                        LoadingState(message = "Obteniendo detalle del producto...")
                    }
                    productoDetailState is UiState.Error -> {
                        ErrorState(
                            message = (productoDetailState as UiState.Error).message,
                            onRetry = {
                                if (localSearchQuery.isNotBlank()) {
                                    productoViewModel.searchByBarcode(localSearchQuery)
                                }
                            }
                        )
                    }
                    // Lógica original para la lista de búsqueda
                    else -> {
                        when (val state = searchState) {
                            is UiState.Idle -> {
                                EmptyState(
                                    icon = Icons.Default.Search,
                                    title = "Busca un producto",
                                    message = "Usa la cámara para escanear un código o realiza una búsqueda manual"
                                )
                            }

                            is UiState.Loading -> {
                                LoadingState(message = "Buscando productos...")
                            }

                            is UiState.Success -> {
                                if (state.data.isEmpty()) {
                                    EmptyState(
                                        icon = Icons.Default.Search,
                                        title = "Sin resultados",
                                        message = "No se encontraron productos con \"$searchQuery\"",
                                        actionButton = {
                                            SecondaryButton(
                                                text = "Nueva búsqueda",
                                                onClick = {
                                                    localSearchQuery = ""
                                                    productoViewModel.clearSearch()
                                                }
                                            )
                                        }
                                    )
                                } else {
                                    Column {
                                        // Header de resultados
                                        Text(
                                            text = "${state.data.size} resultado(s) encontrado(s)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(16.dp)
                                        )

                                        // Lista de productos
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(state.data) { producto ->
                                                ProductoCard(
                                                    producto = producto,
                                                    onClick = {
                                                        onNavigateToDetail(producto.sku)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is UiState.Error -> {
                                ErrorState(
                                    message = state.message,
                                    onRetry = {
                                        productoViewModel.searchProductos(localSearchQuery)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    // El diálogo InfoDialog no se está usando, puedes eliminarlo si quieres.
    // Lo mantengo aquí por si lo necesitas en el futuro.
    InfoDialog(
        title = "Escanear Código",
        message = "La funcionalidad de escaneo con cámara estará disponible próximamente. Por ahora, utiliza la búsqueda manual.",
        onDismiss = { showScanDialog = false },
        showDialog = showScanDialog
    )

    // Escáner de códigos de barras (se muestra condicionalmente)
    if (showScanner) {
        BarcodeScanner(
            onBarcodeScanned = { codigo ->
                // Cuando se escanea un código, buscar automáticamente
                localSearchQuery = codigo
                productoViewModel.searchByBarcode(codigo)
                showScanner = false
            },
            onClose = {
                showScanner = false
            }
        )
    }
}


